package com.metrink.grammar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanArgument implements Argument {
    public static final Logger LOG = LoggerFactory.getLogger(BooleanArgument.class);

    public static final BooleanArgument True = new BooleanArgument(true);
    public static final BooleanArgument False = new BooleanArgument(false);

    private final boolean argument;

    public BooleanArgument(final boolean argument) {
        this.argument = argument;
    }

    public boolean getBoolean() {
        return argument;
    }

    @Override
    public String toString() {
        return argument + "";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final BooleanArgument rhs = (BooleanArgument) obj;

        return new EqualsBuilder()
                      .append(argument, rhs.argument)
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(argument)
          .toHashCode();
    }
}
