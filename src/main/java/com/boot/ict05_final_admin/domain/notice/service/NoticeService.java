package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.fcm.dto.NoticeFcmEvent;
import com.boot.ict05_final_admin.domain.fcm.service.HqNoticeFcmBridgeService;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeSearchDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeAttachment;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeStatus;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeAttachmentRepository;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 공지사항 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * <p>공지사항 등록, 수정, 조회, 첨부파일 처리 등의 기능을 제공한다.</p>
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeAttachmentRepository noticeAttachmentRepository;
    private final NoticeAttachmentService noticeAttachmentService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 새로운 공지사항을 등록하고 첨부파일을 저장한다.
     *
     * @param dto   공지사항 등록 정보 (제목, 내용, 카테고리, 우선순위, 작성자 등)
     * @param files 첨부파일 리스트 (없을 수 있음)
     * @return 저장된 공지사항 ID
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
    public Long insertOfficeNotice(NoticeWriteFormDTO dto, List<MultipartFile> files) throws Exception {
        Notice notice = Notice.builder()
                .memberIdFk(dto.getMemberIdFk())
                .noticeCategory(dto.getNoticeCategory())
                .noticePriority(dto.getNoticePriority())
                .noticeStatus(NoticeStatus.ACTIVE)
                .isShow(dto.getIsShow())
                .title(dto.getTitle())
                .body(dto.getBody())
                .writer(dto.getWriter())
                .registeredAt(LocalDateTime.now())
                .build();

        // DB 저장
        Notice saved = noticeRepository.save(notice);
        Long id = saved.getId();

        // 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileUrl = noticeAttachmentService.uploadFile(file); // S3, 로컬 등
                    NoticeAttachment attachment = new NoticeAttachment();
                    attachment.setNoticeId(saved.getId());
                    attachment.setUrl(fileUrl);
                    attachment.setOriginalFilename(file.getOriginalFilename());
                    noticeAttachmentRepository.save(attachment);
                }
            }
        }
        eventPublisher.publishEvent(
                new NoticeFcmEvent(id, NoticeFcmEvent.Type.CREATED)
        );

        return id;
    }

    /**
     * 작성자 이름으로 필터링하여 공지사항 목록을 페이지 단위로 조회한다.
     *
     * @param noticeSearchDTO   작성자 이름 (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 공지사항 리스트 DTO
     */
    public Page<NoticeListDTO> selectAllOfficeNotice(NoticeSearchDTO noticeSearchDTO, Pageable pageable) {
        return noticeRepository.listNotice(noticeSearchDTO, pageable);
    }

    /**
     * ID를 기준으로 공지사항을 조회한다.
     *
     * @param id 공지사항 ID
     * @return 공지사항 엔티티, 존재하지 않으면 null
     */
    public Notice findById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    /**
     * 기존 공지사항을 수정하고 첨부파일을 추가 저장한다.
     *
     * @param dto   수정할 공지사항 정보
     * @param files 첨부파일 리스트 (없을 수 있음)
     * @return 수정된 공지사항 엔티티
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
    public Notice noticeModify(NoticeModifyFormDTO dto, List<MultipartFile> files) throws Exception {
        Notice notice = findById(dto.getId());
        if (notice == null) throw new IllegalArgumentException("게시물이 없습니다.");

        notice.updateNotice(dto);

        // 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileUrl = noticeAttachmentService.uploadFile(file); // S3, 로컬 등
                    NoticeAttachment attachment = new NoticeAttachment();
                    attachment.setNoticeId(notice.getId());
                    attachment.setUrl(fileUrl);
                    attachment.setOriginalFilename(file.getOriginalFilename());
                    noticeAttachmentRepository.save(attachment);
                }
            }
        }

        eventPublisher.publishEvent(
                new NoticeFcmEvent(dto.getId(), NoticeFcmEvent.Type.UPDATED)
        );

        return notice;
    }

    /**
     * 공지사항 상세 정보를 조회한다.
     *
     * @param id 공지사항 ID
     * @return 공지사항 엔티티, 존재하지 않으면 null
     */
    public Notice detailNotice(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }


    /**
     * 공지사항 id 를 받아 삭제하는 프로세스
     * @param id
     */
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }


    /**
     * 공지사항 액셀 다운로드
     * @return
     * @throws IOException
     */
    public byte[] downloadExcel(NoticeSearchDTO noticeSearchDTO, Pageable pageable)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Sheet1");

        Row sheet_row = sheet.createRow(0);
        sheet_row.createCell(0).setCellValue("ID");
        sheet_row.createCell(1).setCellValue("제목");
        sheet_row.createCell(2).setCellValue("작성자");
        sheet_row.createCell(3).setCellValue("작성일");

        long count = noticeRepository.countNotice(noticeSearchDTO);
        PageRequest pageRequest = PageRequest.of(0, (int) count, Sort.by("id").descending());
        Page<NoticeListDTO> lists = noticeRepository.listNotice(noticeSearchDTO, pageRequest);

        int i = 1;
        for (NoticeListDTO notice : lists) {
            Row sheet1_row = sheet.createRow(i);
            sheet1_row.createCell(0).setCellValue(notice.getId());
            sheet1_row.createCell(1).setCellValue(notice.getTitle());
            sheet1_row.createCell(2).setCellValue(notice.getWriter());
            sheet1_row.createCell(3).setCellValue(notice.getRegisteredAt());
            i++;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}
