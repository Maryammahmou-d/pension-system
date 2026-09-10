package com.rubix.pension.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration for the whole API. Without this, only endpoints explicitly annotated
 * with {@code @CrossOrigin} (previously just {@code UserController}) were reachable from the
 * frontend's origin — every other controller (including all termination/report/download
 * endpoints) had no CORS headers at all, so browsers silently blocked the response (JSON and,
 * worse, binary file downloads like PDFs/Excel/ZIP would come back as empty/corrupt blobs
 * client-side even though the backend actually generated them successfully).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_ORIGINS = {"http://localhost:3000", "http://localhost:3001"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
