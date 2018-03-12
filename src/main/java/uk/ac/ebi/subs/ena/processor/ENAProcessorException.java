package uk.ac.ebi.subs.ena.processor;

public class ENAProcessorException extends RuntimeException {

    public ENAProcessorException(Exception sourceException) {
        super(sourceException.getMessage(),sourceException);
    }
}
