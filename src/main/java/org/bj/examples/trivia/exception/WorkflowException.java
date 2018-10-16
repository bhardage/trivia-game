package org.bj.examples.trivia.exception;

public class WorkflowException extends Exception {
    public WorkflowException() {
        super();
    }

    public WorkflowException(final String message) {
        super(message);
    }

    public WorkflowException(final Throwable cause) {
        super(cause);
    }

    public WorkflowException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
