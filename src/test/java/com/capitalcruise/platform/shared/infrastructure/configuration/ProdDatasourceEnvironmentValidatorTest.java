package com.capitalcruise.platform.shared.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class ProdDatasourceEnvironmentValidatorTest {

    private final ProdDatasourceEnvironmentValidator validator = new ProdDatasourceEnvironmentValidator();

    @Test
    void shouldFailInProdWhenSupabasePasswordIsMissing() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty("SUPABASE_DB_PASSWORD", "   ");

        assertThatThrownBy(() -> validator.postProcessEnvironment(environment, new SpringApplication()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("SUPABASE_DB_PASSWORD is required in prod profile");
    }

    @Test
    void shouldNotFailInDevWhenSupabasePasswordIsMissing() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("dev");

        assertThatCode(() -> validator.postProcessEnvironment(environment, new SpringApplication()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptProdEnvironmentWhenCloudRunVariablesArePresent() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty("DATABASE_URL", "jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require");
        environment.setProperty("SUPABASE_DB_USERNAME", "postgres.cbkoepkuqcxiepeqasvr");
        environment.setProperty("SUPABASE_DB_PASSWORD", "SupaCapitalCruisePA$$WORD");

        assertThatCode(() -> validator.postProcessEnvironment(environment, new SpringApplication()))
                .doesNotThrowAnyException();
    }
}
