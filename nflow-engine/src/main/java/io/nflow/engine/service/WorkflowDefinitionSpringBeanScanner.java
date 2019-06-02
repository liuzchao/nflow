package io.nflow.engine.service;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.nflow.engine.workflow.definition.AbstractWorkflowDefinition;
import io.nflow.engine.workflow.definition.WorkflowState;

/**
 * Service for managing workflow definitions.
 */
@Component
public class WorkflowDefinitionSpringBeanScanner {

  private final WorkflowDefinitionService workflowDefinitionService;

  @Inject
  public WorkflowDefinitionSpringBeanScanner(WorkflowDefinitionService workflowDefinitionService) {
    this.workflowDefinitionService = workflowDefinitionService;
  }

  /**
   * Add given workflow definitions to the managed definitions.
   * @param workflowDefinitions The workflow definitions to be added.
   */
  @Autowired(required = false)
  public void setWorkflowDefinitions(Collection<AbstractWorkflowDefinition<? extends WorkflowState>> workflowDefinitions) {
    workflowDefinitions.forEach(workflowDefinitionService::addWorkflowDefinition);
  }

}
