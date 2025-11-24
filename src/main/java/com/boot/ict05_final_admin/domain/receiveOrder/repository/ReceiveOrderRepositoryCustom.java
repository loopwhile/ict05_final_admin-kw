package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.*;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReceiveOrderRepositoryCustom {

    // 수주 목록 조회
    Page<ReceiveOrderListDTO> listReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable);
    // 수주 총 개수
    long countReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO);
    // 수주 상세 조회
    Optional<ReceiveOrderDetailDTO> findDetailById(Long id);
    // 수주 상세 - 주문 상품 리스트
    List<ReceiveOrderItemDTO> findItemsByOrderId(Long id);
    // 상단 카드 데이터
    ReceiveOrderSummaryDTO getSummary();
    // 수주 코드로 상태 변경 시에도 필요
    Optional<ReceiveOrder> findOrderById(Long id);
    // 수주 상태 조건부 변경
    // 주어진 id 와 curr 값이 현재 DB 에 저장된 상태와 일치할 때에만 상태를 next 로 변경. 동시 수정 충돌을 방지
    int updateStatusIfCurrent(Long id, ReceiveOrderStatus curr, ReceiveOrderStatus next);
    // 주문 코드 기준으로 수주 상태 일괄 변경
    int updateStatusByOrderCode(String orderCode, ReceiveOrderStatus status);
}
