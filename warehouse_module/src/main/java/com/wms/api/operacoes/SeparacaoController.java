package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Separacao;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/separacoes")
@Tag(name = "Separações", description = "Gestão de separação de pedidos")
public class SeparacaoController extends BaseCrudController<Separacao> {
    public SeparacaoController(EntityManager entityManager) {
        super(entityManager, Separacao.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar separações", description = "Retorna todas as separações ativas.")
    @ApiResponse(responseCode = "200", description = "Separações listadas com sucesso")
    public List<Separacao> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar separação", description = "Busca uma separação ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Separação encontrada"),
            @ApiResponse(responseCode = "404", description = "Separação não encontrada ou inativa")})
    public ResponseEntity<Separacao> buscarPorId(
            @Parameter(description = "ID da separação", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar separação", description = "Inicia uma separação de pedido.")
    @ApiResponse(responseCode = "201", description = "Separação criada com sucesso")
    public ResponseEntity<Separacao> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da separação", required = true)
            @RequestBody Separacao entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar separação", description = "Atualiza uma separação ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Separação atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Separação não encontrada ou inativa")})
    public ResponseEntity<Separacao> atualizar(
            @Parameter(description = "ID da separação", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da separação", required = true)
            @RequestBody Separacao entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir separação", description = "Desativa a separação sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Separação desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Separação não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da separação", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
