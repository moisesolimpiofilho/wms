package com.wms.repository.operacoes;

import com.wms.domain.operacoes.OrdemEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdemEstoqueRepository extends JpaRepository<OrdemEstoque, Long> {
    Optional<OrdemEstoque> findByCode(String code);
}
