package com.metrink.grammar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerExpression {
    public static final Logger LOG = LoggerFactory.getLogger(TriggerExpression.class);

    private final Comparator comparator;
    private final NumberArgument numberArg;
    private final RelativeTimeArgument timeArg;

    public TriggerExpression(final Comparator comparator, final NumberArgument numberArg) {
        this(comparator, numberArg, null);
    }

    public TriggerExpression(final Comparator comparator,
                             final NumberArgument numberArg,
                             final RelativeTimeArgument timeArg) {
        this.comparator = comparator;
        this.numberArg = numberArg;
        this.timeArg = timeArg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(comparator.toString());
        sb.append(" ");
        sb.append(numberArg.toString());
        sb.append(" for ");
        sb.append(timeArg.toString());
        sb.append(" ");

        return sb.toString();
    }

    public Comparator getComparator() {
        return comparator;
    }

    public NumberArgument getNumberArg() {
        return numberArg;
    }

    public RelativeTimeArgument getTimeArg() {
        return timeArg;
    }
}
