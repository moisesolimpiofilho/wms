package com.wms.api.operacoes;

import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/motivos-movimentacao")
@Tag(name = "Motivos de movimentação", description = "Catálogo fixo de motivos de movimentação de estoque")
public class MotivoMovimentacaoController {
    @GetMapping
    @Operation(summary = "Listar motivos de movimentação", description = "Retorna os motivos fixos disponíveis.")
    @ApiResponse(responseCode = "200", description = "Motivos listados com sucesso")
    public List<MotivoResponse> listar() {
        return Arrays.stream(MotivoMovimentacao.values())
                .map(motivo -> new MotivoResponse(motivo.name(), motivo.getDescricao()))
                .toList();
    }

    public record MotivoResponse(String codigo, String descricao) {
    }
}
