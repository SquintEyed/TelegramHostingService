package org.example.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ErrorDto {

    @JsonProperty(value = "error_description")
    private String errorDescription;

    @JsonProperty(value = "error_status")
    private HttpStatus errorStatus;
}
