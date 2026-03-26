package com.bonitasoft.connectors.yousign;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import static org.assertj.core.api.Assertions.*;

class ActivateConnectorPropertyTest {

    @Property
    void should_reject_blank_apiKey(@ForAll("blankStrings") String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey)
                .baseUrl("https://api-sandbox.yousign.app/v3")
                .signatureRequestId("req-1").build();
        var connector = new ActivateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_signatureRequestId(@ForAll("blankStrings") String reqId) {
        var config = YousignConfiguration.builder().apiKey("key")
                .baseUrl("https://api-sandbox.yousign.app/v3")
                .signatureRequestId(reqId).build();
        var connector = new ActivateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_accept_valid_configuration(
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String apiKey,
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String reqId) {
        var config = YousignConfiguration.builder().apiKey(apiKey)
                .baseUrl("https://api-sandbox.yousign.app/v3")
                .signatureRequestId(reqId).build();
        var connector = new ActivateConnector();
        assertThatCode(() -> connector.validateConfiguration(config)).doesNotThrowAnyException();
    }

    @Property
    void should_reject_null_apiKey() {
        var config = YousignConfiguration.builder().apiKey(null)
                .baseUrl("https://api-sandbox.yousign.app/v3")
                .signatureRequestId("req-1").build();
        var connector = new ActivateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_null_signatureRequestId() {
        var config = YousignConfiguration.builder().apiKey("key")
                .baseUrl("https://api-sandbox.yousign.app/v3")
                .signatureRequestId(null).build();
        var connector = new ActivateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_build_config_with_defaults(@ForAll @StringLength(min = 1, max = 100) String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).signatureRequestId("req-1").build();
        assertThat(config.getConnectTimeout()).isEqualTo(30000);
        assertThat(config.getReadTimeout()).isEqualTo(60000);
    }

    @Property
    void should_preserve_apiKey(@ForAll @StringLength(min = 1, max = 500) String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).build();
        assertThat(config.getApiKey()).isEqualTo(apiKey);
    }

    @Property
    void should_preserve_signatureRequestId(@ForAll @StringLength(min = 1, max = 500) String reqId) {
        var config = YousignConfiguration.builder().apiKey("key").signatureRequestId(reqId).build();
        assertThat(config.getSignatureRequestId()).isEqualTo(reqId);
    }

    @Property
    void should_accept_any_positive_timeout(@ForAll @net.jqwik.api.constraints.IntRange(min = 1, max = 300000) int timeout) {
        var config = YousignConfiguration.builder().apiKey("key").connectTimeout(timeout).build();
        assertThat(config.getConnectTimeout()).isEqualTo(timeout);
    }

    @Property
    void should_accept_any_positive_maxRetries(@ForAll @net.jqwik.api.constraints.IntRange(min = 0, max = 10) int retries) {
        var config = YousignConfiguration.builder().apiKey("key").maxRetries(retries).build();
        assertThat(config.getMaxRetries()).isEqualTo(retries);
    }

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "\t", "\n", null);
    }
}
