package com.metrink.grammar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class StringArgument implements Argument {

    private final String argument;

    public StringArgument(final String argument) {
        this.argument = argument;
    }


    public String getString() {
        return argument;
    }

    @Override
    public String toString() {
        return argument;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final StringArgument rhs = (StringArgument) obj;

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
