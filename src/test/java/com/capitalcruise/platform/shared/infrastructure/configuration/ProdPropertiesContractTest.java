package com.capitalcruise.platform.shared.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ProdPropertiesContractTest {

    @Test
    void shouldKeepPortFallbackInProdProperties() throws Exception {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application-prod.properties")) {
            assertThat(inputStream).isNotNull();
            properties.load(inputStream);
        }

        assertThat(properties.getProperty("spring.datasource.url"))
                .isEqualTo("${DATABASE_URL:jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require}");
        assertThat(properties.getProperty("server.port")).isEqualTo("${PORT:8080}");
        assertThat(properties.getProperty("spring.datasource.username")).isEqualTo("${SUPABASE_DB_USERNAME}");
        assertThat(properties.getProperty("spring.datasource.password")).isEqualTo("${SUPABASE_DB_PASSWORD}");
    }
}
