package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RetryPolicyTest {

    @Test
    void should_execute_successfully_on_first_attempt() throws YousignException {
        var policy = new RetryPolicy(3);
        String result = policy.execute(() -> "success");
        assertThat(result).isEqualTo("success");
    }

    @Test
    void should_return_actual_value_from_callable() throws YousignException {
        var policy = new RetryPolicy(0);
        Integer result = policy.execute(() -> 42);
        assertThat(result).isEqualTo(42);
    }

    @Test
    void should_retry_on_retryable_exception() throws YousignException {
        var sleepTimes = new java.util.ArrayList<Long>();
        var policy = new RetryPolicy(3) {
            @Override
            void sleep(long millis) {
                sleepTimes.add(millis);
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
        assertThat(sleepTimes).hasSize(2);
        // Each sleep time should be positive
        assertThat(sleepTimes).allMatch(t -> t > 0);
    }

    @Test
    void should_retry_exactly_once_when_maxRetries_is_1() throws YousignException {
        var policy = new RetryPolicy(1) {
            @Override
            void sleep(long millis) { }
        };
        var counter = new int[]{0};
        String result = policy.execute(() -> {
            counter[0]++;
            if (counter[0] == 1) {
                throw new YousignException("Retry", 500, true);
            }
            return "ok";
        });
        assertThat(result).isEqualTo("ok");
        assertThat(counter[0]).isEqualTo(2);
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
    void should_not_retry_when_retryable_false() {
        var counter = new int[]{0};
        var policy = new RetryPolicy(5) {
            @Override
            void sleep(long millis) { }
        };
        assertThatThrownBy(() -> policy.execute(() -> {
            counter[0]++;
            throw new YousignException("Not retryable", 400, false);
        })).isInstanceOf(YousignException.class);
        assertThat(counter[0]).isEqualTo(1);
    }

    @Test
    void should_fail_after_max_retries_exceeded() {
        var counter = new int[]{0};
        var policy = new RetryPolicy(2) {
            @Override
            void sleep(long millis) { }
        };
        assertThatThrownBy(() -> policy.execute(() -> {
            counter[0]++;
            throw new YousignException("Rate limit", 429, true);
        })).isInstanceOf(YousignException.class)
           .hasMessageContaining("Rate limit");
        // Initial attempt + 2 retries = 3 total calls
        assertThat(counter[0]).isEqualTo(3);
    }

    @Test
    void should_fail_immediately_when_maxRetries_is_zero() {
        var counter = new int[]{0};
        var policy = new RetryPolicy(0);
        assertThatThrownBy(() -> policy.execute(() -> {
            counter[0]++;
            throw new YousignException("Fail", 502, true);
        })).isInstanceOf(YousignException.class);
        assertThat(counter[0]).isEqualTo(1);
    }

    @Test
    void should_wrap_unexpected_exceptions() {
        var policy = new RetryPolicy(3);
        assertThatThrownBy(() -> policy.execute(() -> {
            throw new RuntimeException("Unexpected");
        })).isInstanceOf(YousignException.class)
           .hasMessageContaining("Unexpected error during API call");
    }

    @Test
    void should_wrap_checked_exceptions() {
        var policy = new RetryPolicy(3);
        assertThatThrownBy(() -> policy.execute(() -> {
            throw new Exception("Some checked exception");
        })).isInstanceOf(YousignException.class)
           .hasMessageContaining("Unexpected error");
    }

    @ParameterizedTest
    @ValueSource(ints = {429, 500, 502, 503})
    void should_identify_retryable_status_codes(int code) {
        assertThat(RetryPolicy.isRetryableStatusCode(code)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 301, 400, 401, 403, 404, 501, 504})
    void should_identify_non_retryable_status_codes(int code) {
        assertThat(RetryPolicy.isRetryableStatusCode(code)).isFalse();
    }

    @Test
    void should_calculate_exponential_wait_at_attempt_0() {
        var policy = new RetryPolicy(3);
        long wait0 = policy.calculateWait(0);
        // Base = 1000 * (1 << 0) = 1000, capped at min(1000, 64000) = 1000
        // jitter in [0, 500), so total in [1000, 1500)
        assertThat(wait0).isGreaterThanOrEqualTo(1000L);
        assertThat(wait0).isLessThan(1500L);
    }

    @Test
    void should_calculate_exponential_wait_at_attempt_1() {
        var policy = new RetryPolicy(3);
        long wait1 = policy.calculateWait(1);
        // Base = 1000 * (1 << 1) = 2000, jitter in [0, 1000)
        assertThat(wait1).isGreaterThanOrEqualTo(2000L);
        assertThat(wait1).isLessThan(3000L);
    }

    @Test
    void should_calculate_exponential_wait_at_attempt_2() {
        var policy = new RetryPolicy(3);
        long wait2 = policy.calculateWait(2);
        // Base = 1000 * (1 << 2) = 4000, jitter in [0, 2000)
        assertThat(wait2).isGreaterThanOrEqualTo(4000L);
        assertThat(wait2).isLessThan(6000L);
    }

    @Test
    void should_cap_wait_at_max_for_high_attempts() {
        var policy = new RetryPolicy(10);
        long waitHigh = policy.calculateWait(10);
        // 1000 * (1 << 10) = 1024000, capped at 64000
        // jitter in [0, 32000), total in [64000, 96000)
        assertThat(waitHigh).isGreaterThanOrEqualTo(64000L);
        assertThat(waitHigh).isLessThan(96000L);
    }

    @Test
    void should_increase_wait_between_attempts() {
        var policy = new RetryPolicy(5);
        // Run multiple times and check minimum bounds increase
        long minWait0 = 1000L; // minimum for attempt 0
        long minWait1 = 2000L; // minimum for attempt 1
        long wait0 = policy.calculateWait(0);
        long wait1 = policy.calculateWait(1);
        assertThat(wait0).isGreaterThanOrEqualTo(minWait0);
        assertThat(wait1).isGreaterThanOrEqualTo(minWait1);
        assertThat(minWait1).isGreaterThan(minWait0);
    }

    @Test
    void should_actually_sleep_in_real_sleep_method() {
        var policy = new RetryPolicy(1);
        long start = System.currentTimeMillis();
        policy.sleep(50);
        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed).isGreaterThanOrEqualTo(30L); // Some tolerance
    }

    @Test
    void should_throw_runtime_exception_when_sleep_interrupted() {
        var policy = new RetryPolicy(1);
        Thread.currentThread().interrupt();
        assertThatThrownBy(() -> policy.sleep(1000))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Retry interrupted");
        // Clear the interrupt flag
        Thread.interrupted();
    }

    @Test
    void should_preserve_interrupt_flag_when_sleep_interrupted() {
        var policy = new RetryPolicy(1);
        Thread.currentThread().interrupt();
        try {
            policy.sleep(1000);
        } catch (RuntimeException e) {
            // Expected
        }
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        // Clear interrupt flag
        Thread.interrupted();
    }

    @Test
    void should_preserve_original_exception_on_retry_exhaustion() {
        var policy = new RetryPolicy(1) {
            @Override
            void sleep(long millis) { }
        };
        var originalException = new YousignException("Original error", 503, true);
        assertThatThrownBy(() -> policy.execute(() -> {
            throw originalException;
        })).isSameAs(originalException);
    }
}
