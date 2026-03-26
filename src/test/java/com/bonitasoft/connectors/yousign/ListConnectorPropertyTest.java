package com.bonitasoft.connectors.yousign;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.constraints.IntRange;
import static org.assertj.core.api.Assertions.*;

class ListConnectorPropertyTest {

    @Property
    void should_reject_blank_apiKey(@ForAll("blankStrings") String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).baseUrl("url").build();
        var connector = new ListConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_accept_valid_configuration(@ForAll @net.jqwik.api.constraints.AlphaChars @StringLength(min = 1, max = 100) String apiKey) {
        var config = YousignConfiguration.builder().apiKey(apiKey).baseUrl("https://api.yousign.app/v3").build();
        var connector = new ListConnector();
        assertThatCode(() -> connector.validateConfiguration(config)).doesNotThrowAnyException();
    }

    @Property
    void should_preserve_pageSize(@ForAll @IntRange(min = 1, max = 100) int pageSize) {
        var config = YousignConfiguration.builder().apiKey("key").pageSize(pageSize).build();
        assertThat(config.getPageSize()).isEqualTo(pageSize);
    }

    @Property
    void should_preserve_sortOrder(@ForAll("sortOrders") String sortOrder) {
        var config = YousignConfiguration.builder().apiKey("key").sortOrder(sortOrder).build();
        assertThat(config.getSortOrder()).isEqualTo(sortOrder);
    }

    @Property
    void should_accept_null_optional_filters() {
        var config = YousignConfiguration.builder().apiKey("key").baseUrl("url").build();
        assertThat(config.getStatusFilter()).isNull();
        assertThat(config.getExternalIdFilter()).isNull();
        assertThat(config.getAfterCursor()).isNull();
    }

    @Property
    void should_preserve_statusFilter(@ForAll @StringLength(min = 1, max = 50) String status) {
        var config = YousignConfiguration.builder().apiKey("key").statusFilter(status).build();
        assertThat(config.getStatusFilter()).isEqualTo(status);
    }

    @Property
    void should_preserve_externalIdFilter(@ForAll @StringLength(min = 1, max = 200) String extId) {
        var config = YousignConfiguration.builder().apiKey("key").externalIdFilter(extId).build();
        assertThat(config.getExternalIdFilter()).isEqualTo(extId);
    }

    @Property
    void should_preserve_afterCursor(@ForAll @StringLength(min = 1, max = 200) String cursor) {
        var config = YousignConfiguration.builder().apiKey("key").afterCursor(cursor).build();
        assertThat(config.getAfterCursor()).isEqualTo(cursor);
    }

    @Property
    void should_reject_null_apiKey() {
        var config = YousignConfiguration.builder().apiKey(null).baseUrl("url").build();
        var connector = new ListConnector();
        assertThatThrownBy(() -> connector.validateConfiguration(config)).isInstanceOf(IllegalArgumentException.class);
    }

    @Property
    void should_default_pageSize_to_20() {
        var config = YousignConfiguration.builder().apiKey("key").build();
        assertThat(config.getPageSize()).isEqualTo(20);
    }

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "\t", "\n", null);
    }

    @Provide
    Arbitrary<String> sortOrders() {
        return Arbitraries.of("asc", "desc");
    }
}
