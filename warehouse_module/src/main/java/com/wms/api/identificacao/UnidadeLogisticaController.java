package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.UnidadeLogistica;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unidades-logisticas")
@Tag(name = "Unidades logísticas", description = "Cadastro e consulta de unidades logísticas")
public class UnidadeLogisticaController extends BaseCrudController<UnidadeLogistica> {
    public UnidadeLogisticaController(EntityManager entityManager) {
        super(entityManager, UnidadeLogistica.class);
    }
}
