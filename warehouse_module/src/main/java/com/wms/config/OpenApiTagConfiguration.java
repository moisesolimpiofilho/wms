package com.wms.config;

// IMPORTS OBRIGATÓRIOS PARA O OPENAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiTagConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            // Define explicitamente o Gateway como servidor padrão das requisições do Swagger
            .servers(List.of(
                new Server().url("http://localhost:8082").description("API Gateway (Porta Pública)")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(
                new Components()
                    .addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            );
    }

    @Bean
    public OpenApiCustomizer entityControllerTags(ObjectProvider<ApplicationContext> applicationContextProvider) {
        return openApi -> {
            ApplicationContext applicationContext = applicationContextProvider.getObject();
            Map<String, String> controllerTags = applicationContext
                    .getBeansWithAnnotation(RestController.class)
                    .values()
                    .stream()
                    .map(AopUtils::getTargetClass)
                    .filter(controller -> controller.isAnnotationPresent(RequestMapping.class))
                    .filter(controller -> controller.isAnnotationPresent(Tag.class))
                    .collect(java.util.stream.Collectors.toMap(
                            controller -> controller.getAnnotation(RequestMapping.class).value()[0],
                            controller -> controller.getAnnotation(Tag.class).name()));

            openApi.getPaths().forEach((path, pathItem) -> {
                String tag = controllerTags.entrySet().stream()
                        .filter(entry -> path.equals(entry.getKey())
                                || path.startsWith(entry.getKey() + "/"))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);

                if (tag != null) {
                    pathItem.readOperations().forEach(operation -> operation.setTags(java.util.List.of(tag)));
                }
            });
        };
    }

    @Bean
    public OpenApiCustomizer forbiddenResponseForProtectedOperations() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    if (operation.getResponses() == null) {
                        operation.setResponses(new ApiResponses());
                    }
                    operation.getResponses().addApiResponse("403",
                            new ApiResponse().description(
                                    "Acesso negado: token de autorização ausente ou inválido"));
                }));
    }
}
