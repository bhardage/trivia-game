package org.bj.examples.trivia.exception;

public class GameNotStartedException extends WorkflowException {
    public GameNotStartedException() {
        super();
    }

    public GameNotStartedException(final String message) {
        super(message);
    }

    public GameNotStartedException(final Throwable cause) {
        super(cause);
    }

    public GameNotStartedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
