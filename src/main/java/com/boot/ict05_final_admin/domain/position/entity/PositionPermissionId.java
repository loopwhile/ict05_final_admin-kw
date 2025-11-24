package com.boot.ict05_final_admin.domain.position.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class PositionPermissionId implements Serializable {
    @Column(name = "position_id_fk", nullable = false)
    private Long positionId;

    @Column(name = "permission_code", length = 100, nullable = false)
    private String permissionCode;
}