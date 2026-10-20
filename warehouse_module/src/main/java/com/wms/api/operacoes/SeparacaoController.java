package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Separacao;
import com.wms.service.operacoes.SeparacaoService;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/separacoes")
@Tag(name = "Separações", description = "Gestão de separação de pedidos")
public class SeparacaoController extends BaseCrudController<Separacao> {
    private final SeparacaoService separacaoService;

    public SeparacaoController(EntityManager entityManager, SeparacaoService separacaoService) {
        super(entityManager, Separacao.class);
        this.separacaoService = separacaoService;
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
            @RequestBody Separacao entity) {
        Separacao separacao = separacaoService.iniciar(entity);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(separacao.getId())
                        .toUri())
                .body(separacao);
    }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Concluir separação", description = "Conclui uma separação em andamento.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Separação atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Separação não encontrada ou inativa"),
            @ApiResponse(responseCode = "409", description = "Separação não está em andamento")})
    public ResponseEntity<Separacao> atualizar(
            @Parameter(description = "ID da separação", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Informe status CONCLUIDO para concluir a separação", required = true)
            @RequestBody Separacao entity) {
        return ResponseEntity.ok(separacaoService.concluir(id, entity));
    }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir separação", description = "Desativa a separação sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Separação desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Separação não encontrada ou inativa"),
            @ApiResponse(responseCode = "409", description = "A separação já possui itens registrados")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da separação", example = "1") @PathVariable Long id) {
        separacaoService.validarExclusao(id);
        return super.excluir(id);
    }
}
