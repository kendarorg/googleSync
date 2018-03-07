package org.enel.utils;

public class GDException extends Exception {
    public GDException(String message, Throwable cause) {
        super(message, cause);
    }

    public GDException(String s) {
        super(s);
    }
}
