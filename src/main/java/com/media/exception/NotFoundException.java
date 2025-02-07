package com.media.exception;

public class NotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private String code;

    public NotFoundException(String message){
        super(message);
    }

    public NotFoundException(String message, String code) {
        super(message);
        this.code = code;
    }
}
