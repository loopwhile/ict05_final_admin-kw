package com.boot.ict05_final_admin.domain.home.controller;

import com.boot.ict05_final_admin.domain.home.dto.DashboardViewDTO;
import com.boot.ict05_final_admin.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 대시보드 화면 컨트롤러.
 *
 * HomeService 에서 대시보드용 집계 데이터를 조회하여
 * Thymeleaf 뷰(home.html)에 모델로 전달한다.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 대시보드 메인 페이지를 렌더링한다.
     *
     * 조회 범위와 필터는 HomeService 내부 정책(예: 최근 6개월, 이번 주 등)을 따른다.
     * 조회된 DTO를 모델에 주입한 뒤 index 템플릿을 반환한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 대시보드 뷰 이름("home")
     */
    @GetMapping("/home")
    public String home(Model model) {

        DashboardViewDTO viewDTO = homeService.buildDashboard();

        // KPI
        model.addAttribute("kpiRevenueThisMonth",  viewDTO.kpiRevenueThisMonth());
        model.addAttribute("kpiActiveStores",      viewDTO.kpiActiveStores());
        model.addAttribute("kpiOrderCount",        viewDTO.kpiOrderCount());
        model.addAttribute("kpiNewStores",         viewDTO.kpiNewStores());
        model.addAttribute("kpiRevenueGrowthPct",  viewDTO.kpiRevenueGrowthPct());

        // 차트들
        model.addAttribute("salesLabels",   viewDTO.salesLabels());
        model.addAttribute("salesValues",   viewDTO.salesValues());
        model.addAttribute("weeklyLabels",  viewDTO.weeklyLabels());
        model.addAttribute("weeklyValues",  viewDTO.weeklyValues());
        model.addAttribute("logiLabels",    viewDTO.logiLabels());
        model.addAttribute("logiValues",    viewDTO.logiValues());

        // 표
        model.addAttribute("storeRows", viewDTO.storeRows());

        return "home/home";
    }

}
