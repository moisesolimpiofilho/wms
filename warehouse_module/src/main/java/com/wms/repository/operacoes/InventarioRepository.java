package com.wms.repository.operacoes;

import com.wms.domain.operacoes.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findByIdAndActiveTrue(Long id);
}
