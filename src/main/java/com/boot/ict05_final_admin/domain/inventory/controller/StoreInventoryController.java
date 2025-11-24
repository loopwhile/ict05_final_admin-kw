package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.service.StoreInventoryService;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 본사가 가맹점 재고 현황을 조회하는 컨트롤러.
 *
 * <p>조회 전용(SSR). 수정/삭제 기능은 포함하지 않는다.</p>
 *
 * @author 김주연
 * @since 2025.10.27
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/inventory/store")
public class StoreInventoryController {

    private final StoreInventoryService storeInventoryService;
    private final StoreService storeService;

    /**
     * 가맹점 재고 목록 화면(SSR).
     *
     * <p>검색 조건과 페이징 정보를 받아 서버 사이드 렌더링으로 목록을 반환한다.</p>
     *
     * @param searchDTO 검색 조건(상태, 검색어 등), 선택
     * @param pageable  페이징 정보(기본 page=1, size=10, id DESC). 1-base 페이지 인덱스를 사용한다.
     * @param model     뷰 모델
     * @param request   현재 요청(페이지네이션 링크 생성을 위해 사용)
     * @return 템플릿 경로 {@code inventory/list_store}
     */
    @GetMapping({"/list"})
    public String listStoreInventories(StoreInventorySearchDTO searchDTO,
                                       @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC)
                                       Pageable pageable,
                                       Model model,
                                       HttpServletRequest request) {

        // 1-base → 0-base 변환
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber() - 1,
                pageable.getPageSize(),
                Sort.by("id").descending()
        );

        // 조회
        Page<StoreInventoryListDTO> inventories =
                storeInventoryService.listStoreInventory(searchDTO, pageRequest);

        // 모델 바인딩
        model.addAttribute("inventories", inventories);
        model.addAttribute("storeInventorySearchDTO", searchDTO);
        model.addAttribute("stores", storeService.findStoreName());
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));

        return "inventory/list_store";
    }
}
