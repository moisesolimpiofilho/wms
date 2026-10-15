package com.wms.api.identificacao;

import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
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
@RequestMapping("/api/movimentacoes-estoque")
@Tag(name = "Movimentações de estoque", description = "Consulta do histórico gerado pelas operações de estoque")
public class MovimentacaoEstoqueController {
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public MovimentacaoEstoqueController(MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Listar movimentações", description = "Retorna o histórico de movimentações de estoque ativas.")
    @ApiResponse(responseCode = "200", description = "Movimentações listadas com sucesso")
    public List<MovimentacaoEstoque> listar() {
        return movimentacaoEstoqueRepository.findAllByActiveTrue();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "Consultar movimentação", description = "Busca uma movimentação ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Movimentação encontrada"),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada ou inativa")})
    public ResponseEntity<MovimentacaoEstoque> buscarPorId(
            @Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id) {
        return ResponseEntity.of(movimentacaoEstoqueRepository.findByIdAndActiveTrue(id));
    }
}
