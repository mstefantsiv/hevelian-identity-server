package com.hevelian.identity.server.config.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.wso2.balana.ParsingException;

import com.hevelian.identity.core.TenantService.TenantNotFoundByDomainException;
import com.hevelian.identity.users.UserService.RolesNotFoundByNameException;

import cz.jirutka.spring.exhandler.RestHandlerExceptionResolver;

@Configuration
@Import(SwaggerConfig.class)
public class RestContextConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private ContentNegotiationManager contentNegotiationManager;

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // TODO possibly configure it through the server configuration.
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(exceptionHandlerExceptionResolver()); // resolves
                                                            // @ExceptionHandler
        resolvers.add(restExceptionResolver());
    }

    // See https://github.com/jirutka/spring-rest-exception-handler for more
    // details.
    @Bean
    public RestHandlerExceptionResolver restExceptionResolver() {
        return RestHandlerExceptionResolver.builder().messageSource(httpErrorMessageSource())
                // Set the default application negotiation mapper and mapping
                // handler adapter. In other case the custom ones will be used
                // with wrong behavior.
                .contentNegotiationManager(contentNegotiationManager)
                .httpMessageConverters(requestMappingHandlerAdapter.getMessageConverters())
                // Set a list of error handlers for all application errors. Not
                // the best place, but this is all htat is provided out of the
                // box by the lib. This functionality will be revisited again
                // when we feel the need.
                .addErrorMessageHandler(ParsingException.class, HttpStatus.UNPROCESSABLE_ENTITY)
                .addErrorMessageHandler(TenantNotFoundByDomainException.class, HttpStatus.NOT_FOUND)
                .addErrorMessageHandler(RolesNotFoundByNameException.class, HttpStatus.NOT_FOUND)
                .addErrorMessageHandler(AccessDeniedException.class, HttpStatus.FORBIDDEN)
                // Add custom response for 500 error
                .addErrorMessageHandler(Exception.class, HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @Bean
    public MessageSource httpErrorMessageSource() {
        ReloadableResourceBundleMessageSource m = new ReloadableResourceBundleMessageSource();
        m.setBasename("classpath:/com/hevelian/identity/server/api/errors/messages");
        m.setDefaultEncoding("UTF-8");
        return m;
    }

    @Bean
    public ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
        // Set the default application negotiation mapper and mapping
        // handler adapter. In other case the custom ones will be used
        // with wrong behavior.
        resolver.setContentNegotiationManager(contentNegotiationManager);
        resolver.setMessageConverters(requestMappingHandlerAdapter.getMessageConverters());
        return resolver;
    }

    /**
     * Configure swagger static content. This is not a part of the SwaggerConfig
     * class as it requires class to be a WebMvcConfigurerAdapter.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }
}