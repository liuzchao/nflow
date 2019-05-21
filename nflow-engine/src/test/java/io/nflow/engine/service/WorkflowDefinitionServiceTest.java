package io.nflow.engine.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import io.nflow.engine.internal.dao.WorkflowDefinitionDao;
import io.nflow.engine.internal.executor.BaseNflowTest;
import io.nflow.engine.workflow.definition.AbstractWorkflowDefinition;
import io.nflow.engine.workflow.definition.WorkflowState;

public class WorkflowDefinitionServiceTest extends BaseNflowTest {

  @Mock
  private ClassPathResource nonSpringWorkflowListing;
  @Mock
  private WorkflowDefinitionDao workflowDefinitionDao;
  @Mock
  private Environment env;
  private WorkflowDefinitionService service;

  @BeforeEach
  public void setup() throws Exception {
    when(env.getRequiredProperty("nflow.definition.persist", Boolean.class)).thenReturn(true);
    String dummyTestClassname = DummyTestWorkflow.class.getName();
    ByteArrayInputStream bis = new ByteArrayInputStream(dummyTestClassname.getBytes(UTF_8));
    when(nonSpringWorkflowListing.getInputStream()).thenReturn(bis);
    service = new WorkflowDefinitionService(workflowDefinitionDao, env);
    service.setWorkflowDefinitions(nonSpringWorkflowListing);
    assertThat(service.getWorkflowDefinitions().size(), is(equalTo(0)));
    service.postProcessWorkflowDefinitions();
    assertThat(service.getWorkflowDefinitions().size(), is(equalTo(1)));
    verify(workflowDefinitionDao).storeWorkflowDefinition(eq(service.getWorkflowDefinitions().get(0)));
  }

  @Test
  public void initDuplicateWorkflows() throws Exception {
    String dummyTestClassname = DummyTestWorkflow.class.getName();
    ByteArrayInputStream bis = new ByteArrayInputStream((dummyTestClassname + "\n" + dummyTestClassname).getBytes(UTF_8));
    when(nonSpringWorkflowListing.getInputStream()).thenReturn(bis);
    IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> service.postProcessWorkflowDefinitions());
    assertThat(thrown.getMessage(), containsString("Both io.nflow.engine.service.DummyTestWorkflow and io.nflow.engine.service.DummyTestWorkflow define same workflow type: dummy"));
  }

  @Test
  public void demoWorkflowLoadedSuccessfully() {
    List<AbstractWorkflowDefinition<? extends WorkflowState>> definitions = service.getWorkflowDefinitions();
    assertThat(definitions.size(), is(equalTo(1)));
  }

  @Test
  public void getWorkflowDefinitionReturnsNullWhenTypeIsNotFound() {
    assertThat(service.getWorkflowDefinition("notFound"), is(nullValue()));
  }

  @Test
  public void getWorkflowDefinitionReturnsDefinitionWhenTypeIsFound() {
    assertThat(service.getWorkflowDefinition("dummy"), is(instanceOf(DummyTestWorkflow.class)));
  }

  @Test
  public void nonSpringWorkflowsAreOptional() throws Exception {
    service = new WorkflowDefinitionService(workflowDefinitionDao, env);
    service.postProcessWorkflowDefinitions();
    assertEquals(0, service.getWorkflowDefinitions().size());
  }
}