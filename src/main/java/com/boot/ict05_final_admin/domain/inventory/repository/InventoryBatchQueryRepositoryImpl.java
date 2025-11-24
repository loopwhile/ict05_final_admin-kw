package com.boot.ict05_final_admin.domain.inventory.repository;

import com.boot.ict05_final_admin.domain.inventory.dto.BatchStatusRowDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.FifoCandidateDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.QInventoryBatch;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 배치 조회/슬라이싱 쿼리 리포지토리 구현.
 *
 * <p>QueryDSL 기반. 인덱스 친 컬럼 기준으로 정렬한다.</p>
 */
@Repository
@RequiredArgsConstructor
public class InventoryBatchQueryRepositoryImpl implements InventoryBatchQueryRepository {

    private final JPAQueryFactory qf;
    private static final QInventoryBatch b = QInventoryBatch.inventoryBatch;

    @Override
    public List<BatchStatusRowDTO> findBatchStatusByMaterial(Long materialId) {
        return qf.select(Projections.constructor(
                        BatchStatusRowDTO.class,
                        b.id, b.lotNo, b.receivedDate, b.expirationDate,
                        b.receivedQuantity, b.quantity, b.unitPrice
                ))
                .from(b)
                .where(b.material.id.eq(materialId))
                .orderBy(
                        b.expirationDate.asc().nullsLast(),
                        b.receivedDate.asc(),
                        b.id.asc()
                )
                .fetch();
    }

    @Override
    public List<FifoCandidateDTO> findAvailableBatchesForFifo(Long materialId) {
        return qf.select(Projections.constructor(
                        FifoCandidateDTO.class,
                        b.id,              // batchId
                        b.lotNo,           // lotNo
                        b.expirationDate,  // expirationDate
                        b.quantity         // available(잔량)
                ))
                .from(b)
                .where(
                        b.material.id.eq(materialId),
                        b.quantity.gt(BigDecimal.ZERO)
                )
                // FIFO: 입고일 오름차순, 동일일이면 PK 오름차순
                .orderBy(b.receivedDate.asc(), b.id.asc())
                .fetch();
    }
}
