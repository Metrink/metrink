package com.metrink.metric;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MetricId} that is used when performing operations to change the display name.
 */
public class DisplayMetricId extends MetricId {
    private static final long serialVersionUID = 1L;
    public static final Logger LOG = LoggerFactory.getLogger(DisplayMetricId.class);

    private String displayName;

    public DisplayMetricId(final MetricId id) {
        super(id);

        if(id instanceof DisplayMetricId) {
            this.displayName = ((DisplayMetricId) id).displayName;
        }
    }

    public MetricId appendDisplayName(final String displayName) {
        final StringBuilder sb = new StringBuilder(super.toString());

        if(this.displayName != null) {
            sb.append(this.displayName);
            sb.append(" ");
        }

        // make sure we have a space in there
        if(!displayName.startsWith(" ")) {
            sb.append(" ");
        }

        sb.append(displayName);

        this.displayName = sb.toString();

        return this;
    }

    @Override
    public String toString() {
        if(displayName == null) {
            return super.toString();
        } else {
            return displayName;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final DisplayMetricId rhs = (DisplayMetricId) obj;

        return new EqualsBuilder()
            .appendSuper(super.equals(rhs))
            .append(displayName, rhs.displayName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .appendSuper(super.hashCode())
            .append(displayName)
            .toHashCode();
      }

}
