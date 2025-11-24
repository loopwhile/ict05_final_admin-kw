package com.boot.ict05_final_admin.domain.notice.controller;

import com.boot.ict05_final_admin.config.ProjectAttribute;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeSearchDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeAttachment;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.service.NoticeAttachmentService;
import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;


/**
 * 관리자 공지사항 관리 컨트롤러
 * <p>
 * 공지사항 작성, 목록 조회, 상세 조회, 수정 화면을 제공한다.
 * 카테고리, 우선순위, 첨부파일 기능을 지원한다.
 */
@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final NoticeAttachmentService noticeAttachmentService;
    private final ProjectAttribute projectAttribute;

    /**
     * 공지사항 작성 화면을 표시한다.
     *
     * @param model 뷰에 전달할 모델 객체
     * @return 공지사항 작성 페이지 뷰 이름
     */
    @GetMapping("/notice/write")
    public String addOfficeNotice(Model model) {
        model.addAttribute("noticeWriteFormDTO", new NoticeWriteFormDTO());
        model.addAttribute("NoticeCategory", NoticeCategory.values());
        model.addAttribute("NoticePriority", NoticePriority.values());

        return "notice/write";
    }

    /**
     * 공지사항 목록을 페이징 처리하여 조회한다.
     *
     * @param noticeSearchDTO   (선택) 작성자 이름으로 검색할 경우 전달되는 값
     * @param pageable 페이지 번호, 크기, 정렬 조건을 포함한 페이징 객체
     * @param model    뷰에 전달할 모델 객체
     * @return 공지사항 목록 페이지 뷰 이름
     */
    @GetMapping("/notice/list")
    public String listOfficeNotice(NoticeSearchDTO noticeSearchDTO,
                                   @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                   Model model,
                                   HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by("id").descending());
        Page<NoticeListDTO> notices = noticeService.selectAllOfficeNotice(noticeSearchDTO, pageRequest);

        model.addAttribute("notices", notices);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("noticeSearchDTO", noticeSearchDTO);

        return "notice/list";
    }

    /**
     * 특정 공지사항의 상세 내용을 조회한다.
     *
     * @param id    공지사항 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 공지사항 상세 페이지 뷰 이름
     */
    @GetMapping("/notice/detail/{id}")
    public String detailOfficeNotice(@PathVariable Long id, Model model) {
        Notice notice = noticeService.detailNotice(id);
        List<NoticeAttachment> attachments = noticeAttachmentService.findByNoticeId(notice.getId());

        model.addAttribute("notice", notice);
        model.addAttribute("attachments", attachments);

        return "notice/detail";
    }

    /**
     * 특정 공지사항의 수정 화면을 표시한다.
     *
     * @param id    공지사항 ID
     * @param model 뷰에 전달할 모델 객체
     * @return 공지사항 수정 페이지 뷰 이름
     */
    @GetMapping("/notice/modify/{id}")
    public String modifyOfficeNotice(@PathVariable Long id, Model model) {
        Notice notice = noticeService.detailNotice(id);
        List<NoticeAttachment> attachments = noticeAttachmentService.findByNoticeId(id);

        model.addAttribute("notice", notice);
        model.addAttribute("NoticeCategory", NoticeCategory.values());
        model.addAttribute("NoticePriority", NoticePriority.values());
        model.addAttribute("attachments", attachments);

        return "notice/modify";
    }


    @GetMapping("/notice/delete/{id}")
    public String deleteOfficeNotice(@PathVariable Long id, Model model) {
        noticeService.deleteNotice(id);
        return "redirect:/notice/list";
    }



}
