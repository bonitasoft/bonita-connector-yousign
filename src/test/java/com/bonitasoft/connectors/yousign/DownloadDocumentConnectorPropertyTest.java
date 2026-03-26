package com.bonitasoft.connectors.yousign;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import static org.assertj.core.api.Assertions.*;

class DownloadDocumentConnectorPropertyTest {

    @Property
    void should_reject_blank_apiKey(@ForAll("blankStrings") String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).baseUrl("url").signatureRequestId("req").build();
        var connector = new DownloadDocumentConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_signatureRequestId(@ForAll("blankStrings") String reqId) {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl("url").signatureRequestId(reqId).build();
        var connector = new DownloadDocumentConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_accept_valid_configuration(@ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String apiKey, @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String reqId) {
        var config = YousignConfiguration.builder().apiKey(apiKey).baseUrl("https://api.yousign.app/v3").signatureRequestId(reqId).build();
        var connector = new DownloadDocumentConnector();
        assertThatCode(() -> connector.validateConfiguration(config)).doesNotThrowAnyException();
    }

    @Property
    void should_accept_null_documentId() {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl("url").signatureRequestId("req").documentId(null).build();
        assertThat(config.getDocumentId()).isNull();
    }

    @Property
    void should_preserve_documentId(@ForAll @StringLength(min = 1, max = 200) String docId) {
        var config = YousignConfiguration.builder().apiKey("key").documentId(docId).build();
        assertThat(config.getDocumentId()).isEqualTo(docId);
    }

    @Property
    void should_reject_null_apiKey() {
        var config = YousignConfiguration.builder().apiKey(null).baseUrl("url").signatureRequestId("req").build();
        var connector = new DownloadDocumentConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_null_signatureRequestId() {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl("url").signatureRequestId(null).build();
        var connector = new DownloadDocumentConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_preserve_apiKey(@ForAll @StringLength(min = 1, max = 500) String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).build();
        assertThat(config.getApiKey()).isEqualTo(apiKey);
    }

    @Property
    void should_accept_any_positive_readTimeout(@ForAll @net.jqwik.api.constraints.IntRange(min = 1, max = 300000) int timeout) {
        var config = YousignConfiguration.builder().apiKey("key").readTimeout(timeout).build();
        assertThat(config.getReadTimeout()).isEqualTo(timeout);
    }

    @Property
    void should_reject_blank_baseUrl(@ForAll("blankStrings") String baseUrl) {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl(baseUrl).signatureRequestId("req").build();
        var connector = new DownloadDocumentConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "\t", "\n", null);
    }
}
