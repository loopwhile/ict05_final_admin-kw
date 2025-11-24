package com.boot.ict05_final_admin.common.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    /** 400 Bad Request: 일반적인 잘못된 요청 */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다."),

    /** 400 Validation 실패: 필드 검증 실패 등 */
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALID-400", "검증 실패"),

    /** 400 타입 불일치: 파라미터 타입이 맞지 않을 때 */
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "TYPE-400", "파라미터 타입 불일치"),

    /** 400 필수 파라미터 누락 */
    MISSING_PARAM(HttpStatus.BAD_REQUEST, "PARAM-400", "필수 파라미터 누락"),

    /** 400 요청 본문(JSON) 파싱 실패 */
    MESSAGE_NOT_READABLE(HttpStatus.BAD_REQUEST, "JSON-400", "요청 본문 파싱 실패"),

    /** 405 허용되지 않은 메서드 (예: POST 만 허용하는데 GET 호출) */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP-405", "허용되지 않은 메서드"),

    /** 415 지원하지 않는 Content-Type */
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "HTTP-415", "지원하지 않는 Content-Type"),

    /** 406 Accept 헤더 등으로 응답 타입 협상이 불가할 때 */
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "HTTP-406", "응답 타입 협상 불가"),

    /** 404 리소스(엔드포인트/데이터)를 찾을 수 없음 */
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-404", "리소스를 찾을 수 없습니다."),

    /** 409 충돌(무결성 위반 등) */
    CONFLICT(HttpStatus.CONFLICT, "COMMON-409", "리소스 충돌"),

    /** 401 인증 필요 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-401", "인증이 필요합니다."),

    /** 403 권한 없음(인가 실패) */
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-403", "접근 불가"),


    /** 500 일반 서버 오류 */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-500", "서버 오류"),

    /** 500 DB 관련 내부 오류 (별도 구분이 필요할 때) */
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB-500", "데이터 처리 오류"),


    /** 400 비즈니스 규칙 위반 (도메인 검증 실패 등) */
    BUSINESS_RULE(HttpStatus.BAD_REQUEST, "BIZ-400", "비즈니스 규칙 위반");

    private final HttpStatus status;

    private final String code;

    private final String defaultMessage;

    /**
     * enum 생성자 (private, enum 내부에서만 호출됨).
     *
     * @param status         HTTP 응답에 사용할 상태 코드
     * @param code           서비스 고유 에러 코드(문자열)
     * @param defaultMessage 기본으로 보여줄 메시지(상황에 따라 전역 핸들러에서 덮어쓰기 가능)
     */
    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /** 에러에 매핑된 HTTP 상태 반환 (예: HttpStatus.BAD_REQUEST) */
    public HttpStatus status() {
        return status;
    }

    /** 내부 식별용 에러 코드 반환 (예: "COMMON-400") */
    public String code() {
        return code;
    }

    /** 기본 메시지 반환. 전역 핸들러에서 message 가 비어있을 때 사용하세요. */
    public String defaultMessage() {
        return defaultMessage;
    }
}

