package com.metrink.grammar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class NumberArgument implements Argument {

    private final Number number;

    public NumberArgument(final Number number) {
        this.number = number;
    }

    public long getLong() {
        return number.longValue();
    }

    public int getInt() {
        return number.intValue();
    }

    public double getDouble() {
        return number.doubleValue();
    }

    @Override
    public String toString() {
        return number.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final NumberArgument rhs = (NumberArgument) obj;

        return new EqualsBuilder()
                      .append(number, rhs.number)
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(number)
          .toHashCode();
    }


}
