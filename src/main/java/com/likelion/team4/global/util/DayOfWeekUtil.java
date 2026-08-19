package com.likelion.team4.global.util;

import java.time.DayOfWeek;

public class DayOfWeekUtil {

    private DayOfWeekUtil() {
    }

    public static String toDayCode(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }
}
