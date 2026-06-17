package com.capitalcruise.platform.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class OpenApiConfiguration {

    private final String serverUrl;

    public OpenApiConfiguration(@Value("${capital-cruise.openapi.server-url:}") String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Capital Cruise API")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));

        if (StringUtils.hasText(serverUrl)) {
            openAPI.addServersItem(new io.swagger.v3.oas.models.servers.Server().url(serverUrl.trim()));
        }
        return openAPI;
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

    @Bean
    public GroupedOpenApi loanQuotesOpenApi() {
        return GroupedOpenApi.builder()
                .group("Loan Quotes")
                .pathsToMatch("/api/v1/loan-quotes/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicQuotesOpenApi() {
        return GroupedOpenApi.builder()
                .group("Public Quotes")
                .pathsToMatch("/api/v1/public/quotes/**")
                .build();
    }
}
