package org.charles.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseErrorMessage {
    private HttpStatus statusCode;
    private String errorMessage;
}
