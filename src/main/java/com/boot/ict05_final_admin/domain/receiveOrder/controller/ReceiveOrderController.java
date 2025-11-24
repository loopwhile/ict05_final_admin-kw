package com.boot.ict05_final_admin.domain.receiveOrder.controller;

import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderDetailDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderListDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSearchDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.ReceiveOrderSummaryDTO;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import com.boot.ict05_final_admin.domain.receiveOrder.service.ReceiveOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 수주(Receive Order) 현황 페이지 컨트롤러.
 *
 * <p>본 컨트롤러는 관리자용 수주 목록 화면을 렌더링하며,
 * 페이징, 검색, 상태 필터링, 상단 요약 카드 데이터를 포함한다.<br>
 * 비동기 처리(API) 대신 서버사이드 렌더링 기반 Thymeleaf 뷰로 응답한다.</p>
 */
@Controller
@RequiredArgsConstructor
public class ReceiveOrderController {

    private final ReceiveOrderService receiveOrderService;

    /**
     * 수주현황 목록을 페이징 처리하여 조회한다.
     *
     * @param receiveOrderSearchDTO   (선택) 가맹점명으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 수주현황 목록 페이지 뷰 이름
     */
    @GetMapping("/receive/list")
    public String listOfficeReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO,
                                    @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable,
                                    Model model,
                                    HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());
        Page<ReceiveOrderListDTO> receiveOrder = receiveOrderService.selectAllOfficeReceive(receiveOrderSearchDTO, pageRequest);

        // 각 주문별 상세DTO 조회
        Map<Long, ReceiveOrderDetailDTO> orderDetails = new LinkedHashMap<>();
        for (ReceiveOrderListDTO listDTO : receiveOrder.getContent()) {
            orderDetails.put(listDTO.getId(), receiveOrderService.getReceiveOrderDetail(listDTO.getId()));
        }

        // 상태 필터 & 페이지 필터
        boolean isFirstLoad = request.getParameter("status") == null
                && request.getParameter("s") == null
                && request.getParameter("page") == null;
        if (receiveOrderSearchDTO.getReceiveOrderStatus() != null &&
                receiveOrderSearchDTO.getReceiveOrderStatus().toString().trim().isEmpty()) {
            receiveOrderSearchDTO.setReceiveOrderStatus(null);
        }

        // 상단 카드 데이터
        ReceiveOrderSummaryDTO summary = receiveOrderService.getSummary();

        model.addAttribute("receiveOrder", receiveOrder);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("receiveOrderSearchDTO", receiveOrderSearchDTO);
        model.addAttribute("summary", summary);
        model.addAttribute("statusList", ReceiveOrderStatus.adminVisibleStatuses());

        return "receive/list";
    }

}
