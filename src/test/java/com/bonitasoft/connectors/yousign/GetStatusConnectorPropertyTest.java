package com.bonitasoft.connectors.yousign;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import static org.assertj.core.api.Assertions.*;

class GetStatusConnectorPropertyTest {

    @Property
    void should_reject_blank_apiKey(@ForAll("blankStrings") String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey)
                .baseUrl("https://api-sandbox.yousign.app/v3").signatureRequestId("req-1").build();
        var connector = new GetStatusConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_signatureRequestId(@ForAll("blankStrings") String reqId) {
        var config = YousignConfiguration.builder().apiKey("key")
                .baseUrl("https://api-sandbox.yousign.app/v3").signatureRequestId(reqId).build();
        var connector = new GetStatusConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_accept_valid_configuration(
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String apiKey,
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String reqId) {
        var config = YousignConfiguration.builder().apiKey(apiKey)
                .baseUrl("https://api-sandbox.yousign.app/v3").signatureRequestId(reqId).build();
        var connector = new GetStatusConnector();
        assertThatCode(() -> connector.validateConfiguration(config)).doesNotThrowAnyException();
    }

    @Property
    void should_reject_null_apiKey() {
        var config = YousignConfiguration.builder().apiKey(null).baseUrl("url").signatureRequestId("req-1").build();
        var connector = new GetStatusConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_null_signatureRequestId() {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl("url").signatureRequestId(null).build();
        var connector = new GetStatusConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_preserve_fields(@ForAll @StringLength(min = 1, max = 100) String apiKey, @ForAll @StringLength(min = 1, max = 100) String reqId) {
        var config = YousignConfiguration.builder().apiKey(apiKey).signatureRequestId(reqId).build();
        assertThat(config.getApiKey()).isEqualTo(apiKey);
        assertThat(config.getSignatureRequestId()).isEqualTo(reqId);
    }

    @Property
    void should_build_config_with_defaults(@ForAll @StringLength(min = 1, max = 100) String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).signatureRequestId("req-1").build();
        assertThat(config.getConnectTimeout()).isEqualTo(30000);
    }

    @Property
    void should_accept_any_valid_baseUrl(@ForAll @StringLength(min = 1, max = 200) String baseUrl) {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl(baseUrl).signatureRequestId("req").build();
        assertThat(config.getBaseUrl()).isEqualTo(baseUrl);
    }

    @Property
    void should_accept_positive_readTimeout(@ForAll @net.jqwik.api.constraints.IntRange(min = 1, max = 300000) int timeout) {
        var config = YousignConfiguration.builder().apiKey("key").readTimeout(timeout).build();
        assertThat(config.getReadTimeout()).isEqualTo(timeout);
    }

    @Property
    void should_reject_blank_baseUrl(@ForAll("blankStrings") String baseUrl) {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl(baseUrl).signatureRequestId("req").build();
        var connector = new GetStatusConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "\t", "\n", null);
    }
}
