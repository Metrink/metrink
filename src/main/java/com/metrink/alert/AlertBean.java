package com.metrink.alert;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "alerts")
public class AlertBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer alertId;

    @Column private String alertQuery;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable=false)
    private Date modifiedTime = new Date(); // cannot use JodaTime with Temporal :-(
    @Column private boolean enabled;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        if(!enabled) {
            sb.append("DISABLED: ");
        }

        sb.append(alertId);
        sb.append(": ");
        sb.append(alertQuery);

        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final AlertBean rhs = (AlertBean) obj;

        return new EqualsBuilder()
                      .appendSuper(super.equals(obj))
                      .append(alertId, rhs.alertId)
                      .append(alertQuery, rhs.alertQuery)
                      .append(modifiedTime, rhs.modifiedTime)
                      .append(enabled, rhs.enabled)
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .appendSuper(super.hashCode())
          .append(alertId)
          .append(alertQuery)
          .append(modifiedTime)
          .append(enabled)
          .toHashCode();
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public String getAlertQuery() {
        return alertQuery;
    }

    public void setAlertQuery(String alertQuery) {
        this.alertQuery = alertQuery;
    }

    @PrePersist
    protected void onCreate() {
        modifiedTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedTime = new Date(); // we always want to update the time
    }

    public Date getModifiedTime() {
        //return new Date(modifiedTime.getTime());
        return new Date();
    }

    public void setModifiedTime(Date modifiedTime) {
        //this.modifiedTime = new Date(modifiedTime.getTime());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
