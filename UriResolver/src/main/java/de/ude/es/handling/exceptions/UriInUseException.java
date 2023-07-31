package de.ude.es.handling.exceptions;

public class UriInUseException extends RuntimeException {
    public UriInUseException(){
        super("The Uri is already used inside the Database");
    }
}
