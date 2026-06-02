package rs.moma.flightforge.model;

public class NoValidBuildException extends RuntimeException {
    public NoValidBuildException(String message) {
        super(message);
    }
}