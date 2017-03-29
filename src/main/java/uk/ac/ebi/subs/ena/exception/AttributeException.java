package uk.ac.ebi.subs.ena.exception;

public class AttributeException extends RuntimeException {
    public AttributeException() {
        super();
    }

    public AttributeException(String message) {
        super(message);
    }

    public AttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttributeException(Throwable cause) {
        super(cause);
    }

    protected AttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
