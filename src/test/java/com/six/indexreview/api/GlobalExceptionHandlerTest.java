package com.six.indexreview.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unexpectedErrorsDoNotExposeExceptionDetails() {
        var response = handler.unexpected(new RuntimeException("jdbc password=secret"));

        assertThat(response.errorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.message()).isEqualTo("Unexpected server error");
        assertThat(response.details()).isEqualTo("Unexpected server error");
        assertThat(response.message()).doesNotContain("secret");
    }
}
