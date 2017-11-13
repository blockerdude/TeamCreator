package Exceptions;

/**
 * Thrown when a player has been found to be used in multiple teams
 */
public class DuplicatedPlayerException extends RuntimeException {

    public DuplicatedPlayerException(String message){
        super(message);
    }
}
