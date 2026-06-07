package com.capitalcruise.platform.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Capital Cruise Backend API")
                        .description("Backend for Capital Cruise vehicle credit simulation platform with DDD, JWT and PostgreSQL")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi iamOpenApi() {
        return GroupedOpenApi.builder()
                .group("Auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi clientsOpenApi() {
        return GroupedOpenApi.builder()
                .group("Clients")
                .pathsToMatch("/api/v1/clients/**")
                .build();
    }

    @Bean
    public GroupedOpenApi vehiclesOpenApi() {
        return GroupedOpenApi.builder()
                .group("Vehicles")
                .pathsToMatch("/api/v1/vehicles/**")
                .build();
    }

    @Bean
    public GroupedOpenApi referenceDataOpenApi() {
        return GroupedOpenApi.builder()
                .group("Reference Data")
                .pathsToMatch("/api/v1/reference/**")
                .build();
    }

    @Bean
    public GroupedOpenApi creditOperationOpenApi() {
        return GroupedOpenApi.builder()
                .group("Credit Operations")
                .pathsToMatch("/api/v1/operations/**")
                .build();
    }
}
