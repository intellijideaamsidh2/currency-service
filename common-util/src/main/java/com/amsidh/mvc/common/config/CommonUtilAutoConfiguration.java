package com.amsidh.mvc.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.amsidh.mvc.common.logging.ServiceMethodLoggingAspect;

import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AutoConfiguration
@ComponentScan(basePackages = "com.amsidh.mvc.common")
public class CommonUtilAutoConfiguration {

    public CommonUtilAutoConfiguration() {
        log.info("CommonUtilAutoConfiguration loaded successfully!");
    }

    @Bean
    @ConditionalOnProperty(name = "common-util.logging.service.enabled", havingValue = "true", matchIfMissing = true)
    public ServiceMethodLoggingAspect serviceMethodLoggingAspect(Tracer tracer) {
        return new ServiceMethodLoggingAspect(tracer);
    }

    @Bean
    @ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
    public OpenAPI commonOpenAPI(
            @Value("${springdoc.info.title:${spring.application.name:Application} API}") String title,
            @Value("${springdoc.info.description:API documentation for ${spring.application.name:application}.}") String description,
            @Value("${springdoc.info.version:${application.version:1.0.0}}") String version,
            @Value("${springdoc.info.termsOfService:}") String termsOfService,
            @Value("${springdoc.info.contact.name:}") String contactName,
            @Value("${springdoc.info.contact.email:}") String contactEmail,
            @Value("${springdoc.info.contact.url:}") String contactUrl,
            @Value("${springdoc.info.license.name:Apache 2.0}") String licenseName,
            @Value("${springdoc.info.license.url:https://www.apache.org/licenses/LICENSE-2.0.html}") String licenseUrl,
            @Value("${springdoc.externalDocs.description:}") String extDesc,
            @Value("${springdoc.externalDocs.url:}") String extUrl) {

        Info info = new Info()
                .title(title)
                .description(description)
                .version(version);

        if (!termsOfService.isEmpty()) info.setTermsOfService(termsOfService);
        if (!contactName.isEmpty() || !contactEmail.isEmpty() || !contactUrl.isEmpty()) {
            info.setContact(new Contact()
                    .name(contactName.isEmpty() ? null : contactName)
                    .email(contactEmail.isEmpty() ? null : contactEmail)
                    .url(contactUrl.isEmpty() ? null : contactUrl));
        }
        info.setLicense(new License().name(licenseName).url(licenseUrl));

        OpenAPI openAPI = new OpenAPI().info(info);
        if (!extDesc.isEmpty() || !extUrl.isEmpty()) {
            openAPI.externalDocs(new ExternalDocumentation()
                    .description(extDesc.isEmpty() ? null : extDesc)
                    .url(extUrl.isEmpty() ? null : extUrl));
        }
        return openAPI;
    }
}