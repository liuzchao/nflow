package io.nflow.engine.service;

import static java.lang.Math.max;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.time.StopWatch;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.nflow.engine.internal.dao.ArchiveDao;
import io.nflow.engine.internal.util.PeriodicLogger;

/**
 * Service for archiving old workflow instances from nflow-tables to nflow_archive-tables.
 */
@Named
public class ArchiveService {
  private static final Logger log = getLogger(ArchiveService.class);

  private final ArchiveDao archiveDao;

  @Inject
  public ArchiveService(ArchiveDao archiveDao) {
    this.archiveDao = archiveDao;
  }

  /**
   * Archive old (whose modified time is earlier than <code>olderThan</code> parameter) and passive (that do not have
   * <code>nextActivation</code>) workflows. Copies workflow instances, workflow instance actions and state variables to
   * corresponding archive tables and removes them from production tables.
   *
   * @param olderThan Passive workflow instances whose modified time is before this will be archived.
   * @param batchSize Number of workflow hierarchies to archive in a single transaction. Typical value is 1-20. This parameter
   * mostly affects on archiving performance.
   * @return Total number of archived workflows.
   */
  @SuppressFBWarnings(value = "BAS_BLOATED_ASSIGNMENT_SCOPE", justification = "periodicLogger is defined in correct scope")
  public int archiveWorkflows(DateTime olderThan, int batchSize) {
    Assert.notNull(olderThan, "olderThan must not be null");
    Assert.isTrue(batchSize > 0, "batchSize must be greater than 0");
    archiveDao.ensureValidArchiveTablesExist();
    log.info("Archiving starting. Archiving passive workflows older than {}, in batches of {}.", olderThan, batchSize);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    List<Long> workflowIds;
    PeriodicLogger periodicLogger = new PeriodicLogger(log, 60);
    int archivedWorkflowsTotal = 0;
    do {
      workflowIds = archiveDao.listArchivableWorkflows(olderThan, batchSize);
      if (workflowIds.isEmpty()) {
        break;
      }
      int archivedWorkflows = archiveDao.archiveWorkflows(workflowIds);
      archivedWorkflowsTotal += archivedWorkflows;

      double timeDiff = max(stopWatch.getTime() / 1000.0, 0.000001);
      log.debug("Archived {} workflows. {} workflows / second. Workflow ids: {}. ", archivedWorkflows, archivedWorkflowsTotal
          / timeDiff, workflowIds);
      periodicLogger.info("Archived {} workflows. Archiving about {} workflows / second.", archivedWorkflows,
          archivedWorkflowsTotal / timeDiff);
    } while (true);

    log.info("Archiving finished. Archived {} workflows in {} seconds.", archivedWorkflowsTotal, stopWatch.getTime() / 1000);
    return archivedWorkflowsTotal;
  }
}
