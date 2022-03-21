package myException;

public class ServiceLayerException extends Exception {
    public ServiceLayerException() {
    }

    public ServiceLayerException(String message) {
        super(message);
    }
}
