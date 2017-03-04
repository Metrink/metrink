package com.metrink.metric;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

@Entity
@Table(name = "users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(User.class);

    // taken, in part from: http://garygregory.wordpress.com/2013/06/18/what-are-the-java-timezone-ids/
    public static final Set<String> TIMEZONES =
            ImmutableSet.<String>builder()
                        .add("UTC")
                        .add("US/Alaska")
                        .add("US/Aleutian")
                        .add("US/Arizona")
                        .add("US/Central")
                        .add("US/East-Indiana")
                        .add("US/Eastern")
                        .add("US/Hawaii")
                        .add("US/Indiana-Starke")
                        .add("US/Michigan")
                        .add("US/Mountain")
                        .add("US/Pacific")
                        .add("US/Pacific-New")
                        .add("US/Samoa")
                        .build();

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created", nullable = false)
    private Date created;

    @Column(name = "lastLogin")
    private Timestamp lastLogin;

    @Column(name = "role")
    private String role;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "defaultDashboardId")
    private Integer defaultDashboardId;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("Username", username)
                .add("Password", password != null ? password.substring(0, 2) + "..." + password.length() : null)
                .add("Name", name )
                .add("Created", created)
                .toString();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * Generate a hash unique to the user which includes nonce as a component.
     * NOTE: This fails closed! Making this fail open is a very bad idea. It will make password resets very insecure!
     *
     * @param nonce the nonce
     * @return the hash
     */
    public String generateHash(final long nonce) {
        checkNotNull(username, "Username must be set");
        try {
            // TODO: Put this in a configuration file
            final byte[] keyBytes = "oiCaitoazah3sheing4Choquo7gu7sha2ohcaisiephohsaiJ6boa2eavuato8goog5Fohkoophie8AagieTa4feethieGhoi0dahyei7Aebi8sohnee3veep3pheing".getBytes();
            final SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            mac.update(Long.toString(nonce).getBytes(Charsets.UTF_8));
            mac.update(username.getBytes(Charsets.UTF_8));

            final byte[] rawHmac = mac.doFinal();

            return new String(new Hex().encode(rawHmac), Charsets.UTF_8);

        } catch (final NoSuchAlgorithmException e) {
            LOG.error("NoSuchAlgorithmException: {}", e.getMessage());
            throw new IllegalStateException("NoSuchAlgorithmException", e);

        } catch (final InvalidKeyException e) {
            LOG.error("InvalidKeyException: {}", e.getMessage());
            throw new IllegalStateException("InvalidKeyException", e);
        }
    }

    /**
     * Get username.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username.
     * @param username the username to set
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * Get password.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password.
     * @param password the password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Get name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name.
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get created.
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Set created.
     * @param created the created to set
     */
    public void setCreated(final Date created) {
        this.created = created;
    }

    /**
     * Get lastLogin.
     * @return the lastLogin
     */
    public Timestamp getLastLogin() {
        return lastLogin;
    }

    /**
     * Set lastLogin.
     * @param lastLogin the lastLogin to set
     */
    public void setLastLogin(final Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Returns the raw string from the database.
     *
     * @return the string from the DB
     */
    public String getRoles() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * Get defaultDashboardId.
     * @return the defaultDashboardId
     */
    public Integer getDefaultDashboardId() {
        return defaultDashboardId;
    }

    /**
     * Set defaultDashboardId.
     * @param integer the defaultDashboardId to set
     */
    public void setDefaultDashboardId(Integer integer) {
        this.defaultDashboardId = integer;
    }

}
