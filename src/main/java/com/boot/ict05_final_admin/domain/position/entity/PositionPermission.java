package com.boot.ict05_final_admin.domain.position.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "position_permission",
        indexes = @Index(name="idx_position_permission__perm", columnList = "permission_code"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PositionPermission {

    @EmbeddedId
    private PositionPermissionId id;

    @MapsId("positionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id_fk",
            foreignKey = @ForeignKey(name="fk_position_permission__position"))
    private Position position;
}