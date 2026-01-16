package Exceptions;

public class NoSpotsAvailableException extends RuntimeException {
    public NoSpotsAvailableException() {
        super("No spots available! ");
    }
}
