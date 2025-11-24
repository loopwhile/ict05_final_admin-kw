package com.boot.ict05_final_admin.domain.nav.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "nav_item",
        uniqueConstraints = @UniqueConstraint(name = "uk_nav_item_code", columnNames = "code")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NavItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nav_item_id")
    private Long id;

    @Column(nullable = false, length = 64)
    private String navItemCode;

    @Column(nullable = false, length = 100)
    private String navItemName;

    @Column(nullable = false, length = 255)
    private String navItemPath;

    // @Builder 기본값 유지
    @Builder.Default
    @Column(nullable = false)
    private boolean navItemEnabled = true;

    @Column(length = 255)
    private String navItemDescription;

    public void toggle() { this.navItemEnabled = !this.navItemEnabled; }
}
