package com.example.ticketsmodule.web.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DurationFormat unit tests")
class DurationFormatTest {

    @Test
    @DisplayName("should return dash for null")
    void shouldReturnDashForNull() {
        assertThat(DurationFormat.humanize(null)).isEqualTo("—");
    }

    @Test
    @DisplayName("should return dash for zero or negative")
    void shouldReturnDashForZeroOrNegative() {
        assertThat(DurationFormat.humanize(0)).isEqualTo("—");
        assertThat(DurationFormat.humanize(-5)).isEqualTo("—");
    }

    @Test
    @DisplayName("should return minutes only")
    void shouldReturnMinutesOnly() {
        assertThat(DurationFormat.humanize(45)).isEqualTo("45 мин");
    }

    @Test
    @DisplayName("should return hours only")
    void shouldReturnHoursOnly() {
        assertThat(DurationFormat.humanize(60)).isEqualTo("1 ч");
        assertThat(DurationFormat.humanize(720)).isEqualTo("12 ч");
        assertThat(DurationFormat.humanize(1500)).isEqualTo("25 ч");
    }

    @Test
    @DisplayName("should return hours and minutes")
    void shouldReturnHoursAndMinutes() {
        assertThat(DurationFormat.humanize(90)).isEqualTo("1 ч 30 мин");
    }
}