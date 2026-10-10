package com.example.ticketsmodule.web.util;

/**
 * Форматирование длительности в человекочитаемый вид.
 *   45   → "45 мин"
 *   60   → "1 ч"
 *   90   → "1 ч 30 мин"
 *   720  → "12 ч"
 *   1500 → "25 ч"
 */
public final class DurationFormat {

    private DurationFormat() {
    }

    public static String humanize(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return "—";
        }
        int h = minutes / 60;
        int m = minutes % 60;

        if (h == 0) {
            return m + " мин";
        }
        if (m == 0) {
            return h + " ч";
        }
        return h + " ч " + m + " мин";
    }
}