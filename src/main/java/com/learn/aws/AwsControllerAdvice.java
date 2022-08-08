package com.learn.aws;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AwsControllerAdvice {

    @ExceptionHandler(value = AmazonS3Exception.class)
    ResponseEntity<String> awsExceptionHandler(AmazonS3Exception e){
        return new ResponseEntity<>(e.getErrorMessage(), HttpStatus.valueOf(e.getStatusCode()));
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<String> genericExceptionHandler(Exception e){
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
