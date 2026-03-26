package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class YousignConfigurationTest {

    @Test
    void should_build_with_defaults() {
        var config = YousignConfiguration.builder()
                .apiKey("key")
                .build();
        assertThat(config.getBaseUrl()).isEqualTo("https://api-sandbox.yousign.app/v3");
        assertThat(config.getConnectTimeout()).isEqualTo(30000);
        assertThat(config.getReadTimeout()).isEqualTo(60000);
        assertThat(config.getMaxRetries()).isEqualTo(3);
        assertThat(config.getPageSize()).isEqualTo(20);
    }

    @Test
    void should_build_with_all_fields() {
        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl("https://api.yousign.app/v3")
                .connectTimeout(5000)
                .readTimeout(10000)
                .templateId("tmpl-1")
                .requestName("Test")
                .signatureRequestId("req-1")
                .maxRetries(5)
                .build();
        assertThat(config.getApiKey()).isEqualTo("key");
        assertThat(config.getBaseUrl()).isEqualTo("https://api.yousign.app/v3");
        assertThat(config.getConnectTimeout()).isEqualTo(5000);
        assertThat(config.getTemplateId()).isEqualTo("tmpl-1");
    }

    @Test
    void should_allow_null_optional_fields() {
        var config = YousignConfiguration.builder()
                .apiKey("key")
                .build();
        assertThat(config.getTemplateId()).isNull();
        assertThat(config.getSignatureRequestId()).isNull();
        assertThat(config.getDocumentId()).isNull();
        assertThat(config.getWebhookEndpointUrl()).isNull();
    }
}
