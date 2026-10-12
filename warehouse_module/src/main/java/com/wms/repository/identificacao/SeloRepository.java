package com.wms.repository.identificacao;

import com.wms.domain.identificacao.Selo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeloRepository extends JpaRepository<Selo, Long> {
    Optional<Selo> findByCode(String code);
}
