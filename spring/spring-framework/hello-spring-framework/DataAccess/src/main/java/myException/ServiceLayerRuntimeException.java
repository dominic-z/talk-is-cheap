package myException;

public class ServiceLayerRuntimeException extends RuntimeException {
    public ServiceLayerRuntimeException() {
    }

    public ServiceLayerRuntimeException(String message) {
        super(message);
    }
}
