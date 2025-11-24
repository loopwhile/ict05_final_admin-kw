package com.boot.ict05_final_admin.common.error;

public class ValidationError {

    public final String field;

    public final Object rejectedValue;

    public final String reason;

    public ValidationError(String field, Object rejectedValue, String reason) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.reason = reason;
    }

}
