package com.bonitasoft.connectors.yousign;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import static org.assertj.core.api.Assertions.*;

class RegisterWebhookConnectorPropertyTest {

    @Property
    void should_reject_blank_apiKey(@ForAll("blankStrings") String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).baseUrl("url").webhookEndpointUrl("https://x.com/wh").build();
        var connector = new RegisterWebhookConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_webhookEndpointUrl(@ForAll("blankStrings") String url) {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl("url").webhookEndpointUrl(url).build();
        var connector = new RegisterWebhookConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_accept_valid_configuration(@ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String apiKey, @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 200) String url) {
        var config = YousignConfiguration.builder().apiKey(apiKey).baseUrl("https://api.yousign.app/v3").webhookEndpointUrl(url).build();
        var connector = new RegisterWebhookConnector();
        assertThatCode(() -> connector.validateConfiguration(config)).doesNotThrowAnyException();
    }

    @Property
    void should_preserve_webhookEndpointUrl(@ForAll @StringLength(min = 1, max = 500) String url) {
        var config = YousignConfiguration.builder().apiKey("key").webhookEndpointUrl(url).build();
        assertThat(config.getWebhookEndpointUrl()).isEqualTo(url);
    }

    @Property
    void should_accept_null_webhookSecret() {
        var config = YousignConfiguration.builder().apiKey("key").webhookEndpointUrl("url").build();
        assertThat(config.getWebhookSecret()).isNull();
    }

    @Property
    void should_preserve_webhookSecret(@ForAll @StringLength(min = 1, max = 200) String secret) {
        var config = YousignConfiguration.builder().apiKey("key").webhookSecret(secret).build();
        assertThat(config.getWebhookSecret()).isEqualTo(secret);
    }

    @Property
    void should_reject_null_apiKey() {
        var config = YousignConfiguration.builder().apiKey(null).baseUrl("url").webhookEndpointUrl("url").build();
        var connector = new RegisterWebhookConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_null_webhookEndpointUrl() {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl("url").webhookEndpointUrl(null).build();
        var connector = new RegisterWebhookConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_preserve_sandbox_flag(@ForAll boolean sandbox) {
        var config = YousignConfiguration.builder().apiKey("key").sandbox(sandbox).build();
        assertThat(config.getSandbox()).isEqualTo(sandbox);
    }

    @Property
    void should_preserve_enabled_flag(@ForAll boolean enabled) {
        var config = YousignConfiguration.builder().apiKey("key").enabled(enabled).build();
        assertThat(config.getEnabled()).isEqualTo(enabled);
    }

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "\t", "\n", null);
    }
}
