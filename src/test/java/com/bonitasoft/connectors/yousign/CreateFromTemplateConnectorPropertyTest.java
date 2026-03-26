package com.bonitasoft.connectors.yousign;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import static org.assertj.core.api.Assertions.*;

class CreateFromTemplateConnectorPropertyTest {

    @Property
    void should_reject_blank_apiKey(@ForAll("blankStrings") String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey)
                .baseUrl("https://api-sandbox.yousign.app/v3").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_templateId(@ForAll("blankStrings") String templateId) {
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId(templateId)
                .requestName("name").signerLabel("label").signerFirstName("fn")
                .signerLastName("ln").signerEmail("e@e.com").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_requestName(@ForAll("blankStrings") String requestName) {
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId("tmpl-1")
                .requestName(requestName).signerLabel("label").signerFirstName("fn")
                .signerLastName("ln").signerEmail("e@e.com").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_signerLabel(@ForAll("blankStrings") String signerLabel) {
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId("tmpl-1")
                .requestName("name").signerLabel(signerLabel).signerFirstName("fn")
                .signerLastName("ln").signerEmail("e@e.com").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_signerFirstName(@ForAll("blankStrings") String signerFirstName) {
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId("tmpl-1")
                .requestName("name").signerLabel("label").signerFirstName(signerFirstName)
                .signerLastName("ln").signerEmail("e@e.com").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_signerLastName(@ForAll("blankStrings") String signerLastName) {
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId("tmpl-1")
                .requestName("name").signerLabel("label").signerFirstName("fn")
                .signerLastName(signerLastName).signerEmail("e@e.com").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_signerEmail(@ForAll("blankStrings") String signerEmail) {
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId("tmpl-1")
                .requestName("name").signerLabel("label").signerFirstName("fn")
                .signerLastName("ln").signerEmail(signerEmail).build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_accept_valid_configuration(
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String apiKey,
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String templateId,
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String requestName) {
        var config = YousignConfiguration.builder().apiKey(apiKey)
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId(templateId)
                .requestName(requestName).signerLabel("label").signerFirstName("fn")
                .signerLastName("ln").signerEmail("e@e.com").build();
        var connector = new CreateFromTemplateConnector();
        assertThatCode(() -> connector.validateConfiguration(config)).doesNotThrowAnyException();
    }

    @Property
    void should_build_configuration_with_any_valid_inputs(
            @ForAll @StringLength(min = 1, max = 200) String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).build();
        assertThat(config.getApiKey()).isEqualTo(apiKey);
        assertThat(config.getBaseUrl()).isNotNull();
    }

    @Property
    void should_reject_null_apiKey() {
        var config = YousignConfiguration.builder().apiKey(null)
                .baseUrl("https://api-sandbox.yousign.app/v3").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "\t", "\n", null);
    }
}
