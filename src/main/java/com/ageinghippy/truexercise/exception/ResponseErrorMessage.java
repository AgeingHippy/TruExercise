package com.ageinghippy.truexercise.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseErrorMessage {
    @Schema(example="400")
    private HttpStatus statusCode;
    @Schema(example="Invalid request payload")
    private String errorMessage;
}
