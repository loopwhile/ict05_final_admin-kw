package com.boot.ict05_final_admin.domain.position.repository;

import com.boot.ict05_final_admin.domain.position.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long>, PositionRepositoryCustom {
}
