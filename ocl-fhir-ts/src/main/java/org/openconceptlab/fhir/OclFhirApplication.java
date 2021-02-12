package org.openconceptlab.fhir;

import org.openconceptlab.fhir.config.Config;
import org.openconceptlab.fhir.controller.OclFhirController;
import org.openconceptlab.fhir.converter.CodeSystemConverter;
import org.openconceptlab.fhir.util.OclFhirUtil;
import org.openconceptlab.fhir.interceptor.OclFhirLoggingInterceptor;
import org.openconceptlab.fhir.model.BaseOclEntity;
import org.openconceptlab.fhir.model.UserProfile;
import org.openconceptlab.fhir.provider.CodeSystemResourceProvider;
import org.openconceptlab.fhir.repository.BaseOclRepository;
import org.openconceptlab.fhir.repository.ConceptRepository;
import org.openconceptlab.fhir.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;

@ServletComponentScan
@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@ComponentScan(basePackageClasses = {
        Config.class,
        OclFhirController.class,
        OclFhirLoggingInterceptor.class,
        BaseOclEntity.class,
        BaseOclRepository.class,
        CodeSystemResourceProvider.class,
        OclFhirRestfulServer.class,
        CodeSystemConverter.class,
        OclFhirUtil.class
})
@EnableJpaRepositories(basePackageClasses = {ConceptRepository.class})
@EntityScan(basePackageClasses = {BaseOclEntity.class})
public class OclFhirApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(OclFhirApplication.class);
    }

    UserProfile oclUser = null;

    @Autowired
    UserRepository userRepository;

    @PostConstruct
    public void init() {
        UserProfile user = userRepository.findByUsername("ocladmin");
        if (user != null) {
            oclUser = user;
        } else {
            throw new InternalError("Can not find ocladmin user.");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(OclFhirApplication.class,args);
    }

    @Bean
    UserProfile getOclUser() {
        return oclUser;
    }


}
