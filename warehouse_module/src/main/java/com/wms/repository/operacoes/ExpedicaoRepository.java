package com.wms.repository.operacoes;

import com.wms.domain.operacoes.Expedicao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpedicaoRepository extends JpaRepository<Expedicao, Long> {
}
