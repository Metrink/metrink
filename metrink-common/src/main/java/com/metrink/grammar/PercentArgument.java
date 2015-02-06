package com.metrink.grammar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.util.FastMath;


public class PercentArgument implements Argument {

    private final double percent;

    public PercentArgument(final Integer percent) {
        this.percent = percent/100;
    }

    public double getPercentAsDecimal() {
        return percent;
    }

    @Override
    public String toString() {
        return FastMath.floor(percent*100) + "%";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final PercentArgument rhs = (PercentArgument) obj;

        return new EqualsBuilder()
                      .append(percent, rhs.percent)
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(percent)
          .toHashCode();
    }

}
