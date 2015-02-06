package com.metrink.grammar;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;


public class RelativeTimeArgument implements Argument {

    private final long duration;
    private final TimeUnit timeUnit;

    public RelativeTimeArgument(final long duration, final TimeUnit timeUnit) {
        checkArgument(duration != 0, "Duration must not be zero");
        checkNotNull(timeUnit, "Time unit cannot be null");

        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public RelativeTimeArgument(final long duration, final String timeUnit) {
        checkArgument(duration != 0, "Duration must not be zero");

        switch(timeUnit) {
        case "s":
            this.duration = duration;
            this.timeUnit = TimeUnit.SECONDS;
            break;

        case "m":
            this.duration = duration;
            this.timeUnit = TimeUnit.MINUTES;
            break;

        case "h":
            this.duration = duration;
            this.timeUnit = TimeUnit.HOURS;
            break;

        case "d":
            this.duration = duration;
            this.timeUnit = TimeUnit.DAYS;
            break;

        case "w":
            this.timeUnit = TimeUnit.DAYS;
            this.duration = duration * 7; // we're just calling a week 7 days
            break;

        default:
            throw new IllegalArgumentException("Invalid time unit");
        }
    }

    public long getTimeInMs() {
        return timeUnit.toMillis(duration);
    }

    public long getDuration() {
        return duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public String toString() {
        return duration + timeUnit.toString();
    }
}
