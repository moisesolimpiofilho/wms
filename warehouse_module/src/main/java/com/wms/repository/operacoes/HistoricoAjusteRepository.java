package com.wms.repository.operacoes;

import com.wms.domain.operacoes.ajustes.HistoricoAjuste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoAjusteRepository extends JpaRepository<HistoricoAjuste, Long> {
}
