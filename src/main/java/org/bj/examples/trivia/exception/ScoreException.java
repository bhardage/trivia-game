package org.bj.examples.trivia.exception;

public class ScoreException extends Exception {
    public ScoreException() {
        super();
    }

    public ScoreException(final String message) {
        super(message);
    }

    public ScoreException(final Throwable cause) {
        super(cause);
    }

    public ScoreException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
