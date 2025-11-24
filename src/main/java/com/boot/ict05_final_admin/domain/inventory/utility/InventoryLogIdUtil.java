package com.boot.ict05_final_admin.domain.inventory.utility;

/**
 * 재고 로그 ID 언래핑 유틸.
 *
 * <p>일부 뷰/전달 경로에서 로그 ID에 유형 프리픽스(버킷)를 가산(예: 1_000_000_000 단위)
 * 하여 전달하는 경우가 있다. 이 유틸은 그런 합성 ID에서 실제 원본 PK를 복원한다.</p>
 *
 * <ul>
 *   <li>규칙: {@code logId >= BASE} 이면 {@code logId % BASE} 로 원본 PK 산출</li>
 *   <li>그 외(작은 값)는 원본 PK로 간주하여 그대로 반환</li>
 * </ul>
 *
 * <p>예) INCOME/OUTGO/ADJUST 등 유형별로 1e9, 2e9… 범위를 할당했다가
 * 단일 숫자로 전달하는 패턴을 복원할 때 사용.</p>
 */
public final class InventoryLogIdUtil {

    /** 유형 프리픽스 경계(1e9). 이 값 이상이면 합성 ID로 간주. */
    private static final long BASE = 1_000_000_000L;

    private InventoryLogIdUtil() {
        // no-op
    }

    /**
     * 합성 로그 ID에서 원본 PK를 추출한다.
     *
     * @param logId 합성 가능 로그 ID(널 불가)
     * @return 원본 PK
     * @throws IllegalArgumentException logId가 null인 경우
     */
    public static long unwrap(Long logId) {
        if (logId == null) {
            throw new IllegalArgumentException("로그 ID가 null 입니다.");
        }
        long v = logId;
        return (v >= BASE) ? (v % BASE) : v;
    }
}
