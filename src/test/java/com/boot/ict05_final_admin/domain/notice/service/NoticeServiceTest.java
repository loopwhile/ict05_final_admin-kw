package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NoticeServiceTest {
    @Autowired
    private NoticeService noticeService;

//    @Test
//    void insertThousandNotices() {
//        for (int i = 1; i <= 1000; i++) {
//            NoticeWriteFormDTO dto = new NoticeWriteFormDTO();
//            dto.setNoticeCategory(NoticeCategory.NORMAL);   // 카테고리 선택
//            dto.setNoticePriority(NoticePriority.NORMAL);   // 우선순위 선택
//            dto.setTitle("테스트 게시물 제목 " + i);
//            dto.setBody("<p>테스트 게시물 내용 " + i + "</p>");
//            dto.setWriter("테스트유저");
//
//            noticeService.insertOfficeNotice(dto);
//
//            if (i % 100 == 0) {
//                System.out.println(i + "개 게시물 저장 완료");
//            }
//        }
//    }
}