package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RetryPolicyTest {

    @Test
    void should_execute_successfully_on_first_attempt() throws YousignException {
        var policy = new RetryPolicy(3);
        String result = policy.execute(() -> "success");
        assertThat(result).isEqualTo("success");
    }

    @Test
    void should_retry_on_retryable_exception() throws YousignException {
        var policy = new RetryPolicy(3) {
            @Override
            void sleep(long millis) {
                // no-op for testing
            }
        };
        var counter = new int[]{0};
        String result = policy.execute(() -> {
            counter[0]++;
            if (counter[0] < 3) {
                throw new YousignException("Rate limit", 429, true);
            }
            return "success";
        });
        assertThat(result).isEqualTo("success");
        assertThat(counter[0]).isEqualTo(3);
    }

    @Test
    void should_fail_immediately_on_non_retryable_exception() {
        var policy = new RetryPolicy(3);
        assertThatThrownBy(() -> policy.execute(() -> {
            throw new YousignException("Auth error", 401, false);
        })).isInstanceOf(YousignException.class)
           .hasMessageContaining("Auth error");
    }

    @Test
    void should_fail_after_max_retries_exceeded() {
        var policy = new RetryPolicy(2) {
            @Override
            void sleep(long millis) {
                // no-op
            }
        };
        assertThatThrownBy(() -> policy.execute(() -> {
            throw new YousignException("Rate limit", 429, true);
        })).isInstanceOf(YousignException.class);
    }

    @Test
    void should_wrap_unexpected_exceptions() {
        var policy = new RetryPolicy(3);
        assertThatThrownBy(() -> policy.execute(() -> {
            throw new RuntimeException("Unexpected");
        })).isInstanceOf(YousignException.class)
           .hasMessageContaining("Unexpected error");
    }

    @Test
    void should_identify_retryable_status_codes() {
        assertThat(RetryPolicy.isRetryableStatusCode(429)).isTrue();
        assertThat(RetryPolicy.isRetryableStatusCode(500)).isTrue();
        assertThat(RetryPolicy.isRetryableStatusCode(502)).isTrue();
        assertThat(RetryPolicy.isRetryableStatusCode(503)).isTrue();
        assertThat(RetryPolicy.isRetryableStatusCode(400)).isFalse();
        assertThat(RetryPolicy.isRetryableStatusCode(401)).isFalse();
        assertThat(RetryPolicy.isRetryableStatusCode(404)).isFalse();
    }

    @Test
    void should_calculate_exponential_wait() {
        var policy = new RetryPolicy(3);
        long wait0 = policy.calculateWait(0);
        long wait1 = policy.calculateWait(1);
        // Exponential: 1s, 2s, 4s base + jitter
        assertThat(wait0).isBetween(1000L, 1500L);
        assertThat(wait1).isBetween(2000L, 3000L);
    }
}
