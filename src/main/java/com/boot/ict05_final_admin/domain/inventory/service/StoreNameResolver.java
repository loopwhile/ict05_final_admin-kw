package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 가맹점명 조회 공통 리졸버.
 *
 * <p>표준 JPA 메서드(findById, findAllById)만 사용하여
 * 저장소 구현체 세부 명세에 의존하지 않는다.</p>
 *
 * <p>규칙:</p>
 * <ul>
 *   <li>단일 조회 실패 시: ID(문자열)로 대체</li>
 *   <li>다건 조회: 존재하는 항목만 매핑</li>
 *   <li>다건 + 폴백: 누락 키를 ID(문자열)로 채움</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class StoreNameResolver {

    private final StoreRepository storeRepository;

    /**
     * 단일 ID → 이름.
     *
     * @param storeId 가맹점 ID (null 허용 → null 반환)
     * @return 이름 또는 폴백(ID 문자열) 또는 null
     */
    public String resolveOrFallback(Long storeId) {
        if (storeId == null) return null;
        return storeRepository.findById(storeId)
                .map(Store::getName)
                .orElse(String.valueOf(storeId));
    }

    /**
     * 복수 ID → 이름 맵.
     *
     * <p>존재하지 않는 키는 포함하지 않는다.</p>
     *
     * @param storeIds 가맹점 ID 컬렉션 (null/빈 허용)
     * @return {id → name} 맵
     */
    public Map<Long, String> resolveAll(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return Collections.emptyMap();

        // null ID 제거 및 중복 ID 허용(첫 값 우선)
        List<Long> ids = storeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return storeRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        Store::getId,
                        Store::getName,
                        (a, b) -> a,           // 중복 키 병합 전략: 최초 값 유지
                        HashMap::new
                ));
    }

    /**
     * 복수 ID → 이름 맵(폴백 포함).
     *
     * <p>누락 키는 ID 문자열로 채운다.</p>
     *
     * @param storeIds 가맹점 ID 컬렉션 (null/빈 허용)
     * @return {id → name or idString} 맵
     */
    public Map<Long, String> resolveAllWithFallback(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return Collections.emptyMap();

        // 기본 매핑
        Map<Long, String> map = new HashMap<>(resolveAll(storeIds));

        // 폴백 채우기
        for (Long id : storeIds) {
            if (id == null) continue;
            map.putIfAbsent(id, String.valueOf(id));
        }
        return map;
    }
}
