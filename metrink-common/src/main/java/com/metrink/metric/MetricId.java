package com.metrink.metric;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "unique_metrics_view")
public class MetricId implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final Logger LOG = LoggerFactory.getLogger(MetricId.class);

    @Id @Column private String device;
    @Id @Column private String groupName;
    @Id @Column private String name;

    /**
     * Hibernate Constructor.
     */
    public MetricId() {
    }

    public MetricId(final String device,
                    final String group,
                    final String name) {
        this.device = device;
        this.groupName = group;
        this.name = name;
    }

    public MetricId(final MetricId metricId) {
        this.device = new String(metricId.device);
        this.groupName = new String(metricId.groupName);
        this.name = new String(metricId.name);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();

        sb.append(getDevice());
        sb.append(":");
        sb.append(getGroupName());
        sb.append(":");
        sb.append(getName());

        return sb.toString();
    }

    /**
     * Checks to see if one metric matches another.
     *
     * @param id must be a MetricId that does NOT contain any *.
     * @return true if this MetricId matches passed MetricId.
     */
    public boolean matches(final MetricId id) {
        if (id.getDevice().contains("*") || id.getGroupName().contains("*") || id.getName().contains("*")) {
            throw new IllegalArgumentException("Passed id cannot contain a *");
        }

        final Pattern devicePattern = Pattern.compile(device.replace(".", "\\.").replace("*", ".*"));
        final Pattern groupPattern = Pattern.compile(groupName.replace(".", "\\.").replace("*", ".*"));
        final Pattern namePattern = Pattern.compile(name.replace(".", "\\.").replace("*", ".*"));

        return devicePattern.matcher(id.getDevice()).matches()
                && groupPattern.matcher(id.getGroupName()).matches()
                && namePattern.matcher(id.getName()).matches();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        final MetricId rhs = (MetricId) obj;

        return new EqualsBuilder()
            .append(getDevice(), rhs.getDevice())
            .append(getGroupName(), rhs.getGroupName())
            .append(getName(), rhs.getName())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getDevice())
            .append(getGroupName())
            .append(getName())
            .toHashCode();
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(final String device) {
        this.device = device;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
