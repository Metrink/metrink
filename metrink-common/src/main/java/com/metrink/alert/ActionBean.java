package com.metrink.alert;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "actions")
public class ActionBean implements Serializable {
    //private static final Logger LOG = LoggerFactory.getLogger(ActionBean.class);

    private static final long serialVersionUID = 1L;

    public static final String[] ACTION_TYPES =
        { "Email",
          "AT&T SMS",
          "Sprint SMS",
          "T-Mobile SMS",
          "Verizon SMS" };



    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Integer actionId;

    @Column private String actionName;
    @Column private String type;
    @Column private String value;

    public ActionBean() {
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(actionId);
        sb.append(": ");
        sb.append(actionName);
        sb.append(" ");
        sb.append(type);
        sb.append(" ");
        sb.append(value);

        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }

        final ActionBean rhs = (ActionBean) obj;

        return new EqualsBuilder()
                      //.append(owner, rhs.owner)
                      .append(actionId, rhs.actionId)
                      .append(actionName, rhs.actionName)
                      .append(type, rhs.type)
                      .append(value, rhs.value)
                      .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          //.append(owner)
          .append(actionId)
          .append(actionName)
          .append(type)
          .append(value)
          .toHashCode();
      }

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
