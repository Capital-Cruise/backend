package com.capitalcruise.platform.shared.infrastructure.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.util.StringUtils;

public class ProdDatasourceEnvironmentValidator implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }

        String password = environment.getProperty("SUPABASE_DB_PASSWORD");
        if (!StringUtils.hasText(password)) {
            throw new IllegalStateException("SUPABASE_DB_PASSWORD is required in prod profile");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
