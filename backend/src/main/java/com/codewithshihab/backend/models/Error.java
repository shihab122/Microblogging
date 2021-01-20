package com.codewithshihab.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Error implements Serializable {
    /**
     * 400 Bad Request — Client sent an invalid request — such as lacking required request body or parameter
     * 401 Unauthorized — Client failed to authenticate with the server
     * 403 Forbidden — Client authenticated but does not have permission to access the requested resource
     * 404 Not Found — The requested resource does not exist
     * 412 Precondition Failed — One or more conditions in the request header fields evaluated to false
     * 500 Internal Server Error — A generic error occurred on the server
     * 503 Service Unavailable — The requested service is not available
     */
    private String code;
    private String field;
    private String message;
    private String description;

}
