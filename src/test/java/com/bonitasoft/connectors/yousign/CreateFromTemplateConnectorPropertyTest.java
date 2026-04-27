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
                .requestName("name").build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_requestName(@ForAll("blankStrings") String requestName) {
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId("tmpl-1")
                .requestName(requestName).build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_reject_blank_templatePlaceholdersJson(@ForAll("blankStrings") String placeholders) {
        // templatePlaceholdersJson is now mandatory: signer data lives inside it.
        var config = YousignConfiguration.builder().apiKey("valid-key")
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId("tmpl-1")
                .requestName("name")
                .templatePlaceholdersJson(placeholders)
                .build();
        var connector = new CreateFromTemplateConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("templatePlaceholdersJson");
    }

    @Property
    void should_accept_valid_configuration(
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String apiKey,
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String templateId,
            @ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String requestName) {
        var config = YousignConfiguration.builder().apiKey(apiKey)
                .baseUrl("https://api-sandbox.yousign.app/v3").templateId(templateId)
                .requestName(requestName)
                .templatePlaceholdersJson("{\"signers\":[{\"placeholder_name\":\"s1\"}]}")
                .build();
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
