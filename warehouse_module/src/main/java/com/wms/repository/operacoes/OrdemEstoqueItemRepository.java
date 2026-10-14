package com.wms.repository.operacoes;

import com.wms.domain.operacoes.complementos.OrdemEstoqueItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemEstoqueItemRepository extends JpaRepository<OrdemEstoqueItem, Long> {
}
