package org.openconceptlab.fhir.config;

import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * The Configuration class.
 * @author hp11
 */
@Configuration
public class Config {

    @Bean
    public ImplicitNamingStrategy implicit() {
        return new ImplicitNamingStrategyLegacyJpaImpl();
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.indentOutput(true);
        return builder;
    }

}
