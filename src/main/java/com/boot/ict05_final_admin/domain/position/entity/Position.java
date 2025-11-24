package com.boot.ict05_final_admin.domain.position.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "position_role",
        uniqueConstraints = @UniqueConstraint(name="uq_position_code", columnNames = "position_code"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ← AUTO_INCREMENT
    @Column(name = "position_id")
    private Long id;

    @Column(name = "position_code", length = 50, nullable = false)
    private String code; // OWNER, HR, OPS ...

    @Column(name = "position_name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 255)
    private String description;
}
