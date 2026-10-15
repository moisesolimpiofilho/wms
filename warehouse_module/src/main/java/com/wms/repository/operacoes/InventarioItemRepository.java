package com.wms.repository.operacoes;

import com.wms.domain.operacoes.itens.InventarioItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventarioItemRepository extends JpaRepository<InventarioItem, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from InventarioItem item where item.id = :id and item.active = true")
    Optional<InventarioItem> findActiveByIdForUpdate(@Param("id") Long id);
}
