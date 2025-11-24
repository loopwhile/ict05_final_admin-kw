package com.boot.ict05_final_admin.domain.fcm.repository;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * FCM 전송 로그 엔티티에 대한 JPA 리포지토리 인터페이스.
 *
 * <p>기본 CRUD는 JpaRepository가 제공하며, 커스텀 조회는 {@link FcmSendLogRepositoryCustom}에 정의된다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public interface FcmSendLogRepository extends JpaRepository<FcmSendLog, Long>, FcmSendLogRepositoryCustom { }
