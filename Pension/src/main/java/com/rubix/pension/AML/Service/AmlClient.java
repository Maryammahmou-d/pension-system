package com.rubix.pension.AML.Service;

import com.rubix.pension.AML.Response.AmlCheckResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AmlClient {

    private static final Logger log = LoggerFactory.getLogger(AmlClient.class);

    private final RestTemplate restTemplate;

    @Value("${aml.base-url:http://localhost:8081}")
    private String amlApiUrl;

    @Value("${aml.api-key:}")
    private String amlApiKey;

    public AmlClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void logConfig() {
        log.info("AML client configured: url={}, apiKeyConfigured={}", amlApiUrl, StringUtils.hasText(amlApiKey));
    }

    public AmlCheckResponse screenEmployee(String name, String nationalID) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(amlApiKey)) {
            headers.set("X-API-Key", amlApiKey);
        } else {
            log.warn("AML API key is not configured; the AML backend will reject this request");
        }

        Map<String, Object> body = Map.of(
                "name", name,
                "nationalId", nationalID,
                "sourceSystem", "PENSION_SYSTEM"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = amlApiUrl + "/api/aml/check";

        try {
            return restTemplate.postForObject(url, request, AmlCheckResponse.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AML returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AML returned " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("Cannot reach AML backend at {}: {}", url, e.getMessage());
            throw new RuntimeException("Cannot reach AML backend at " + url + ": " + e.getMessage(), e);
        }
    }

}
