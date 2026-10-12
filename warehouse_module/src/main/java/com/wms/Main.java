package com.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Warehouse Management System API",
        version = "1.0.0",
        description = "API REST para cadastros, estoque, identificação, operações e auditoria do WMS."))
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}