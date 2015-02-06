package com.metrink.dashboard;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean used to represent a {@link MetricOwner}s dashboard.
 */
@Entity
@Table(name = "dashboards")
public class DashboardBean implements Serializable {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(DashboardBean.class);
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer dashboardId;

    @Column private String dashboardName;
    @Column private String definition;

    public DashboardBean() {
    }

    public DashboardBean(int id) {
        this.dashboardId = id;
    }

    @Override
    public String toString() {
        return dashboardName + ": " + definition;
    }

    /**
     * Get the dashboard id.
     * @return the dashboardId
     */
    public Integer getDashboardId() {
        return dashboardId;
    }

    /**
     * Set the dashboard id.
     * @param dashboardId the dashboardId to set
     */
    public void setDashboardId(final Integer dashboardId) {
        this.dashboardId = dashboardId;
    }

    /**
     * Get the dashboard id.
     * @return the dashboardName
     */
    public String getDashboardName() {
        return dashboardName;
    }

    /**
     * Set the dashboard name.
     * @param dashboardName the dashboardName to set
     */
    public void setDashboardName(final String dashboardName) {
        this.dashboardName = dashboardName;
    }

    /**
     * Get YAML definition for the dashboard.
     * @return the definition
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * Set YAML definition for the dashboard.
     * @param definition the definition to set
     */
    public void setDefinition(final String definition) {
        this.definition = definition;
    }

    /**
     * Generate a URL stub from the dashboard name. This is lossy and should never be authoritative.
     * @return a URL stub
     */
    public String getStubName() {
        return getDashboardName().toLowerCase().replaceAll("[^a-z0-9]+", " ").trim().replace(" ", "-");
    }
}
