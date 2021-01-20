package com.codewithshihab.backend.exception;

import com.codewithshihab.backend.models.Error;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExecutionFailureException extends Exception{
    private Error error;

    public ExecutionFailureException(Error error) {
        super();
        this.error = error;
    }
}
