package com.boot.ict05_final_admin.domain.notice.repository;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryCustom {

}
