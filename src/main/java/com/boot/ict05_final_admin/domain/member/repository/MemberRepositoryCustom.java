package com.boot.ict05_final_admin.domain.member.repository;

import com.boot.ict05_final_admin.domain.member.dto.MemberListDTO;
import com.boot.ict05_final_admin.domain.member.dto.MemberSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<MemberListDTO> listMember(MemberSearchDTO memberSearchDTO, Pageable pageable);
}
