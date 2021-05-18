package jp.classmethod.samplerelyingparty.exception;

public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(int statusCode, String message, Throwable t) {
        super(message, t);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
