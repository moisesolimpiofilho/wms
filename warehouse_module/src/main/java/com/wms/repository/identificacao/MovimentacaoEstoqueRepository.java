package com.wms.repository.identificacao;

import com.wms.domain.identificacao.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findAllByActiveTrue();

    Optional<MovimentacaoEstoque> findByIdAndActiveTrue(Long id);
}
