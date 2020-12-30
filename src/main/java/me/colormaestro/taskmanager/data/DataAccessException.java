package me.colormaestro.taskmanager.data;

public class DataAccessException extends Exception {

    public DataAccessException(String message) {
            super(message);
        }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
