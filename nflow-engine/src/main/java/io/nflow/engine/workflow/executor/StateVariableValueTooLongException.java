package io.nflow.engine.workflow.executor;

public class StateVariableValueTooLongException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public StateVariableValueTooLongException(String message) {
    super(message);
  }
}
