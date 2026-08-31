package com.example.Exception;

import org.springframework.http.HttpStatusCode;

public interface BaseErrorCode {

    String getCode();

    String getMessage();

    HttpStatusCode getStatusCode();
}
