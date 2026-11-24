package exception;

/**
 * Base custom exception for all CyberPulse domain errors.
 */
public class CyberPulseException extends RuntimeException {
    public CyberPulseException(String message) {
        super(message);
    }

    public CyberPulseException(String message, Throwable cause) {
        super(message, cause);
    }
}
