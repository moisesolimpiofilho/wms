package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Segmento;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/segmentos")
@Tag(name = "Segmentos", description = "Cadastro de segmentos de produtos")
public class SegmentoController extends BaseCrudController<Segmento> {
    public SegmentoController(EntityManager entityManager) {
        super(entityManager, Segmento.class);
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar segmentos", description = "Retorna todos os segmentos ativos.")
    @ApiResponse(responseCode = "200", description = "Segmentos listados com sucesso")
    public List<Segmento> listar() { return super.listar(); }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Consultar segmento", description = "Busca um segmento ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Segmento encontrado"),
            @ApiResponse(responseCode = "404", description = "Segmento não encontrado ou inativo")})
    public ResponseEntity<Segmento> buscarPorId(
            @Parameter(description = "ID do segmento", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar segmento", description = "Cria um segmento; o ID do corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Segmento criado com sucesso")
    public ResponseEntity<Segmento> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do segmento", required = true)
            @RequestBody Segmento entity) { return super.criar(entity); }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar segmento", description = "Atualiza um segmento ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Segmento atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Segmento não encontrado ou inativo")})
    public ResponseEntity<Segmento> atualizar(
            @Parameter(description = "ID do segmento", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do segmento", required = true)
            @RequestBody Segmento entity) { return super.atualizar(id, entity); }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir segmento", description = "Desativa o segmento sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Segmento desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Segmento não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do segmento", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
