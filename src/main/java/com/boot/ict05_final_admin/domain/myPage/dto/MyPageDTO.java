package com.boot.ict05_final_admin.domain.myPage.dto;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import lombok.*;

/**
 * 마이페이지 정보 조회 및 수정용 DTO
 *
 * <p>회원의 기본 정보를 클라이언트에 전달하거나 수정 요청 시 서버로 전달하는 데이터 전송 객체(DTO)이다.</p>
 *
 * <p>주요 용도:</p>
 * <ul>
 *     <li>마이페이지 조회 시 회원 정보를 View에 전달</li>
 *     <li>회원 정보 수정 시 요청 데이터를 서비스 계층으로 전달</li>
 * </ul>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li><b>id</b> — 회원 고유 식별자 (PK)</li>
 *     <li><b>name</b> — 회원 이름</li>
 *     <li><b>email</b> — 회원 이메일 주소 (로그인 ID로 사용)</li>
 *     <li><b>phone</b> — 회원 전화번호</li>
 *     <li><b>memberImagePath</b> - 회원 프로필 이미지 경로</li>
 * </ul>
 *
 * <p>또한 {@link #fromEntity(Member)} 정적 메서드를 통해
 * {@link Member} 엔티티를 DTO 객체로 변환할 수 있다.</p>
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageDTO {

    /** 회원 ID */
    private Long id;

    /** 회원 이름 */
    private String name;

    /** 회원 이메일 */
    private String email;

    /** 회원 전화번호 */
    private String phone;

    /** 회원 프로필 이미지 경로 */
    private String memberImagePath;

    /**
     * Entity → DTO 변환 메서드
     */
    public static MyPageDTO fromEntity(Member member) {
        return MyPageDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .memberImagePath(member.getMemberImagePath())
                .build();
    }
}
