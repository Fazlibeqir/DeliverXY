package com.deliverXY.backend.NewCode.common.constants;

import java.time.LocalTime;

public final class TimeConstants {

    private TimeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Peak Hours (Morning)
    public static final LocalTime MORNING_PEAK_START = LocalTime.of(7, 30);
    public static final LocalTime MORNING_PEAK_END = LocalTime.of(9, 30);

    // Peak Hours (Evening)
    public static final LocalTime EVENING_PEAK_START = LocalTime.of(16, 30);
    public static final LocalTime EVENING_PEAK_END = LocalTime.of(19, 0);

    // Night Hours
    public static final LocalTime NIGHT_START = LocalTime.of(23, 0);
    public static final LocalTime NIGHT_END = LocalTime.of(6, 0);
}