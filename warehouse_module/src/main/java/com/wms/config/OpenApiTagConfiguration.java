package com.wms.config;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Configuration
public class OpenApiTagConfiguration {
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
}
