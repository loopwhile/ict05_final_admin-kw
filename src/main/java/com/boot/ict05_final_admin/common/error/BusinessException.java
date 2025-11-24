package com.boot.ict05_final_admin.common.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    private final boolean noStackTrace;
    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, false);
    }
    public BusinessException(ErrorCode errorCode, String message, boolean noStackTrace) {
        // cause=null, enableSuppression=true, writableStackTrace = !noStackTrace
        super(message, null, true, !noStackTrace);
        this.errorCode = errorCode;
        this.noStackTrace = noStackTrace;
    }
    public ErrorCode getErrorCode() {

        return errorCode;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {

        return noStackTrace ? this : super.fillInStackTrace();
    }
}
