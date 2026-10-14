package br.unesp.wms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Dados para autenticação e registro de usuários")
public class AuthRequest {
    
    @Schema(description = "Nome de usuário", example = "user6")
    private String username;

    @Schema(description = "Senha do usuário", example = "555")
    private String password;
}
