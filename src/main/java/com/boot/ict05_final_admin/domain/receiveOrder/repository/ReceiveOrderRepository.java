package com.boot.ict05_final_admin.domain.receiveOrder.repository;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiveOrderRepository extends JpaRepository<ReceiveOrder, Long>, ReceiveOrderRepositoryCustom {

    int updateStatusIfCurrent(Long id, ReceiveOrderStatus curr, ReceiveOrderStatus next);
    int updateStatusByOrderCode(String orderCode, ReceiveOrderStatus status);
}
