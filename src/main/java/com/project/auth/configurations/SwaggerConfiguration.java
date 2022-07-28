package com.project.auth.configurations;

import com.project.auth.constants.documentation.GeneralApiConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi apiController() {
        return GroupedOpenApi.builder()
                .group("Auth JWT")
                .packagesToScan("com.project.auth")
                .build();
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(GeneralApiConstants.API_NAME_SWAGGER)
                        .description(GeneralApiConstants.API_DESCRIPTION_SWAGGER)
                        .version("v0.0.1"));
    }
}

