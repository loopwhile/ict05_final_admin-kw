package com.boot.ict05_final_admin.domain.receiveOrder.dto;

import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import lombok.Data;

@Data
public class ReceiveOrderSearchDTO {
    private String s;
    private String type;
    private String size = "10";

    /** 수주 상태 필터 (RECEIVED/SHIPPING/DELIVERED/CANCELED) */
    private ReceiveOrderStatus receiveOrderStatus;
}
