package com.metrink.grammar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Comparator {

    public static final Comparator GREATER_THAN = new Comparator(Type.GREATER_THAN);
    public static final Comparator GREATER_EQUAL = new Comparator(Type.GREATER_EQUAL);
    public static final Comparator LESS_THAN = new Comparator(Type.LESS_THAN);
    public static final Comparator LESS_EQUAL = new Comparator(Type.LESS_EQUAL);
    public static final Comparator EQUAL = new Comparator(Type.EQUAL);

    private enum Type {
        GREATER_THAN,
        GREATER_EQUAL,
        LESS_THAN,
        LESS_EQUAL,
        EQUAL
    }

    private final Type type;

    public Comparator(final String comparator) throws MetrinkParseException {
        switch(comparator) {
        case ">":
            type = Type.GREATER_THAN;
            break;

        case ">=":
            type = Type.GREATER_EQUAL;
            break;

        case "<":
            type = Type.LESS_THAN;
            break;

        case "<=":
            type = Type.LESS_EQUAL;
            break;

        case "==":
            type = Type.EQUAL;
            break;

        case "=": // special case for this, help the user
            throw new MetrinkParseException("Unknown comparator = did you mean ==?");

        default:
            throw new MetrinkParseException("Unknown comparator: " + comparator);
        }
    }

    public Comparator(final Type type) {
        this.type = type;
    }

    /**
     * Checks to see if the comparator is satisfied: value1 comparator value2.
     * @param value1 the first value
     * @param value2 the second value
     * @return true if the comparator is satisfied.
     */
    public boolean compare(final double value1, final double value2) {
        switch(type) {
        case GREATER_THAN:
            return value1 > value2;

        case GREATER_EQUAL:
            return value1 >= value2;

        case LESS_THAN:
            return value1 < value2;

        case LESS_EQUAL:
            return value1 <= value2;

        case EQUAL:
            return value1 == value2;
        }

        return false;
    }

    @Override
    public String toString() {
        switch(type) {
        case GREATER_THAN:
            return ">";

        case GREATER_EQUAL:
            return ">=";

        case LESS_THAN:
            return "<";

        case LESS_EQUAL:
            return "<=";

        case EQUAL:
            return "==";
        }

        return "?";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final Comparator rhs = (Comparator) obj;

        return new EqualsBuilder()
                      .append(type, rhs.type)
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(type)
          .toHashCode();
    }

}
