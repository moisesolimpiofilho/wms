package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Recebimento;
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
@RequestMapping("/api/recebimentos")
@Tag(name = "Recebimentos", description = "Gestão de recebimentos de fornecedores")
public class RecebimentoController extends BaseCrudController<Recebimento> {
    public RecebimentoController(EntityManager entityManager) {
        super(entityManager, Recebimento.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar recebimentos", description = "Retorna todos os recebimentos ativos.")
    @ApiResponse(responseCode = "200", description = "Recebimentos listados com sucesso")
    public List<Recebimento> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar recebimento", description = "Busca um recebimento ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Recebimento encontrado"),
            @ApiResponse(responseCode = "404", description = "Recebimento não encontrado ou inativo")})
    public ResponseEntity<Recebimento> buscarPorId(
            @Parameter(description = "ID do recebimento", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar recebimento", description = "Registra um recebimento de fornecedor.")
    @ApiResponse(responseCode = "201", description = "Recebimento criado com sucesso")
    public ResponseEntity<Recebimento> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do recebimento", required = true)
            @RequestBody Recebimento entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar recebimento", description = "Atualiza um recebimento ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Recebimento atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recebimento não encontrado ou inativo")})
    public ResponseEntity<Recebimento> atualizar(
            @Parameter(description = "ID do recebimento", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do recebimento", required = true)
            @RequestBody Recebimento entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir recebimento", description = "Desativa o recebimento sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Recebimento desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recebimento não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do recebimento", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
