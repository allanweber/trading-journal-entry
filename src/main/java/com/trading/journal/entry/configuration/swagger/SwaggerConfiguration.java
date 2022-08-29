package com.trading.journal.entry.configuration.swagger;

import com.trading.journal.entry.ApplicationException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
@EnableWebMvc
@NoArgsConstructor
public class SwaggerConfiguration implements WebMvcConfigurer {

    private static final List<Response> GLOBAL_RESPONSES = Arrays.asList(
            new ResponseBuilder().code("400")
                    .description("Invalid data provided").build(),
            new ResponseBuilder().code("401")
                    .description("Unauthorized access").build(),
            new ResponseBuilder().code("403")
                    .description("Access forbidden to the resource").build(),
            new ResponseBuilder().code("404")
                    .description("Resource not found").build(),
            new ResponseBuilder().code("500")
                    .description("Server undefined exception").build());

    private static final String API_BASE_PACKAGE = "com.trading.journal.entry.api";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(API_BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .pathMapping("/")
                .securityContexts(singletonList(securityContext()))
                .securitySchemes(singletonList(apiKey()))
                .useDefaultResponseMessages(false)
                .globalResponses(HttpMethod.GET, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.POST, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.PUT, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.PATCH, GLOBAL_RESPONSES)
                .globalResponses(HttpMethod.DELETE, GLOBAL_RESPONSES);
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("Allan Weber", "https://allanweber.dev", "a.cassianoweber@gmail.com");
        return new ApiInfo(
                "Entries for Trade Journal",
                "A HTTP REST API to manage Trade Journal Entries.",
                "1.0",
                "termsOfService",
                contact,
                "MIT", "", Collections.emptyList());
    }

    private ApiKey apiKey() {
        return new ApiKey("Bearer Token", AUTHORIZATION_HEADER, "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return singletonList(new SecurityReference("Bearer Token", authorizationScopes));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html**")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    //This is a hack to fix some current swagger issue for spring 2.6+
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream().filter(mapping -> mapping.getPatternParser() == null).toList();
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    assert field != null;
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw (ApplicationException) new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()).initCause(e);
                }
            }
        };
    }
}