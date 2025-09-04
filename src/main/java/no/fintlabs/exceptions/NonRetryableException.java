package no.fintlabs.exceptions;

public class NonRetryableException extends RuntimeException {
    public NonRetryableException(String message) {
        super(message, null, false, false);
    }
}
