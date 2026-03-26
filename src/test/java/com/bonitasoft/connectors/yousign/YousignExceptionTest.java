package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class YousignExceptionTest {

    @Test
    void should_create_with_message_only() {
        var ex = new YousignException("test error");
        assertThat(ex.getMessage()).isEqualTo("test error");
        assertThat(ex.getStatusCode()).isEqualTo(-1);
        assertThat(ex.isRetryable()).isFalse();
    }

    @Test
    void should_create_with_message_and_cause() {
        var cause = new RuntimeException("root cause");
        var ex = new YousignException("test error", cause);
        assertThat(ex.getMessage()).isEqualTo("test error");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getStatusCode()).isEqualTo(-1);
        assertThat(ex.isRetryable()).isFalse();
    }

    @Test
    void should_create_with_status_code_and_retryable() {
        var ex = new YousignException("rate limit", 429, true);
        assertThat(ex.getStatusCode()).isEqualTo(429);
        assertThat(ex.isRetryable()).isTrue();
    }

    @Test
    void should_create_with_all_parameters() {
        var cause = new RuntimeException("root");
        var ex = new YousignException("error", 500, true, cause);
        assertThat(ex.getStatusCode()).isEqualTo(500);
        assertThat(ex.isRetryable()).isTrue();
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    void should_not_be_retryable_for_client_errors() {
        var ex = new YousignException("bad request", 400, false);
        assertThat(ex.isRetryable()).isFalse();
    }
}
