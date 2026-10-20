package com.wms.api.estoque;

import com.wms.domain.estoque.Estoque;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estoques")
@Tag(name = "Estoques", description = "Consulta de saldos atualizados pelos fluxos operacionais")
public class EstoqueController {
    private final EntityManager entityManager;

    public EstoqueController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Listar estoques", description = "Retorna todos os registros de estoque ativos.")
    @ApiResponse(responseCode = "200", description = "Estoques listados com sucesso")
    public List<Estoque> listar() {
        return entityManager.createQuery(
                        "select estoque from Estoque estoque where estoque.active = true",
                        Estoque.class)
                .getResultList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "Consultar estoque", description = "Busca um registro de estoque ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Estoque encontrado"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado ou inativo")})
    public ResponseEntity<Estoque> buscarPorId(
            @Parameter(description = "ID do registro de estoque", example = "1") @PathVariable Long id) {
        Estoque estoque = entityManager.find(Estoque.class, id);
        return estoque == null || !Boolean.TRUE.equals(estoque.getActive())
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(estoque);
    }
}
