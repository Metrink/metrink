package com.metrink.grammar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class Conjunction {
    
    public static final Conjunction AND = new Conjunction(Type.AND);
    public static final Conjunction OR = new Conjunction(Type.OR);
    
    private enum Type {
        AND,
        OR
    }
    
    private final Type type;
    
    public Conjunction(final String conjunction) throws MetrinkParseException{
        switch(conjunction) {
        case "and":
            type = Type.AND;
            break;
            
        case "or":
            type = Type.OR;
            break;
            
        default:
            throw new MetrinkParseException("Unknown conjunction: " + conjunction);
        }
    }
    
    public Conjunction(final Type type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final Conjunction rhs = (Conjunction) obj;

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
