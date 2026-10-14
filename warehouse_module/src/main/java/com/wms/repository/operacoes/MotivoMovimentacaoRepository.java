package com.wms.repository.operacoes;

import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MotivoMovimentacaoRepository extends JpaRepository<MotivoMovimentacao, Long> {
    Optional<MotivoMovimentacao> findByCode(String code);
}
