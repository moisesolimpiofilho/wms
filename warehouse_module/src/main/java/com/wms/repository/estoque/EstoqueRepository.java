package com.wms.repository.estoque;

import com.wms.domain.estoque.Estoque;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select estoque from Estoque estoque
            where estoque.productId = :productId
              and estoque.locationId = :locationId
              and ((:batchId is null and estoque.batchId is null) or estoque.batchId = :batchId)
              and ((:logisticUnitId is null and estoque.logisticUnitId is null)
                   or estoque.logisticUnitId = :logisticUnitId)
              and estoque.active = true
            order by estoque.id
            """)
    List<Estoque> lockActiveByProductAndLocation(
            @Param("productId") Long productId,
            @Param("batchId") Long batchId,
            @Param("locationId") Long locationId,
            @Param("logisticUnitId") Long logisticUnitId);
}
