package com.boot.ict05_final_admin.common.error;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class ErrorResponse {

    public final OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

    public final String requestId;
    public final int status;
    public final String code;
    public final String message;
    public final String path;
    public final String method;
    public final List<ValidationError> errors;

    public ErrorResponse(String requestId, int status, String code, String message,
                         String path, String method, List<ValidationError> errors) {
        this.requestId = requestId;
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.method = method;
        this.errors = errors;
    }

    public static ErrorResponse of(String requestId, ErrorCode ec, String message,
                                   String path, String method, List<ValidationError> errors) {
        return new ErrorResponse(
                requestId,
                ec.status().value(),
                ec.code(),
                (message == null || message.isBlank()) ? ec.defaultMessage() : message,
                path,
                method,
                errors
        );
    }
}