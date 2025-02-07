package com.media.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadRequestException extends RuntimeException{
    private String code;
    
    public BadRequestException(String message){
        super(message);
    }

    public BadRequestException(String code, String message) {
        super(message);
        this.code = code;
    }
}
