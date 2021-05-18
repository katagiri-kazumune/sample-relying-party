package jp.classmethod.samplerelyingparty.exception;

public class AuthorizationException extends RuntimeException {

    private final int statusCode;

    public AuthorizationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public AuthorizationException(int statusCode, String message, Throwable t) {
        super(message, t);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
