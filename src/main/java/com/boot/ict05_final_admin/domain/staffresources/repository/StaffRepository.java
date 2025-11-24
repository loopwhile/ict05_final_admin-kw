package com.boot.ict05_final_admin.domain.staffresources.repository;

import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<StaffProfile, Long>, StaffRepositoryCustom {

    // @OneToOne private Member member; 라면, 컬럼명이 member_id_fk여도 프로퍼티 경로는 member.id
    Optional<StaffProfile> findByMember_Id(Long memberId);

    boolean existsByMember_Id(Long memberId);

    // 기존 EntityGraph 대신 명시적 LEFT JOIN FETCH
    @Query("""
    select s from StaffProfile s
    left join fetch s.store st
    where s.id = :id
    """)
    Optional<StaffProfile> findWithStoreByIdLeft(@Param("id") Long id);

    // 네이티브로 물리 테이블 존재 확인
    @Query(value = "select * from staff_profile where staff_id = :id", nativeQuery = true)
    Optional<StaffProfile> findRaw(@Param("id") Long id);
}
