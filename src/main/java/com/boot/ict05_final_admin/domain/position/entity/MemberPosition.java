package com.boot.ict05_final_admin.domain.position.entity;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_position",
        indexes = {
                @Index(name="idx_member_position__member", columnList = "member_id_fk"),
                @Index(name="idx_member_position__position", columnList = "position_id_fk")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberPosition {

    @EmbeddedId
    private MemberPositionId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id_fk",
            foreignKey = @ForeignKey(name="fk_member_position__member"))
    private Member member;

    @MapsId("positionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id_fk",
            foreignKey = @ForeignKey(name="fk_member_position__position"))
    private Position position;
}