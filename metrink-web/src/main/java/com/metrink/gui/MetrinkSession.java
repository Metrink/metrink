package com.metrink.gui;

import java.io.ObjectInputStream;
import java.security.MessageDigest;
import java.sql.SQLException;

import org.apache.commons.codec.Charsets;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.metrink.metric.User;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanHandler;

/**
 * Wicket authenticated session for Metrink. Obtain through ((MetrinkSession)getSession()) within components.
 */
public class MetrinkSession extends AbstractAuthenticatedWebSession {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(MetrinkSession.class);

    private User user;
    private String username;

    private QueryRunner queryRunner;

    public MetrinkSession() {
        super(null);
        queryRunner = null;
    }

    @Inject
    public MetrinkSession(final QueryRunner queryRunner,
                          @Assisted final Request request) {
        super(request);
        this.queryRunner = queryRunner;
    }

    /**
     * Returns the currently logged in user or null if not logged in.
     * @return the currently logged in user or null.
     */
    public static User getCurrentUser() {
        return ((MetrinkSession)Session.get()).getUser();
    }

    /**
     * Gets the roles for the currently logged in user or an empty set of roles.
     * @return the roles for the currently logged in user.
     */
    public static MetrinkRoles getCurrentRoles() {
        final User user = getCurrentUser();

        return user == null ? new MetrinkRoles(null) : new MetrinkRoles(user.getRoles());
    }


    @Override
    public Roles getRoles() {
        return isSignedIn() ? new MetrinkRoles(getUser().getRoles()) : new Roles();
    }

    @Override
    public boolean isSignedIn() {
        return user != null;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        user = null;
        username = null;
    }

    /**
     * Perform authentication.
     * @param username the candidate username
     * @param password the candidate password
     * @return true if authenticated.
     */
    public boolean login(final String username, final String password) {
        if (isValid(username, password)) {
            LOG.debug("Successful auth for user {}", username);
            this.username = username;
            return true;
        }
        return false;
    }

    /**
     * Checks to see if the entered username and password are correct.
     * @param username the username.
     * @param password the password.
     * @return true if they are correct, false otherwise.
     */
    private boolean isValid(final String username, final String password) {
        user = getUserByUsername(username);

        if (user == null) {
            LOG.debug("User {} not found", username);
            return false;
        }

        if (!MessageDigest.isEqual(user.getPassword().getBytes(Charsets.UTF_8), password.getBytes(Charsets.UTF_8))) {
            LOG.debug("Password mismatch for {}", username);
            return false;
        }

        // update the last login time
        try {
            queryRunner.update("update users set lastLogin = now() where username = :username")
                         .bind("username", user.getUsername())
                         .execute();
        } catch (final SQLException e) {
            LOG.error("Error updating lastLogin: {}", e.getMessage());
            // we don't throw here, because this isn't a show-stopper
        }

        LOG.debug("Authenticated {}", username);
        return true;
    }

    private User getUserByUsername(final String username) {
        try {
            final User ret = queryRunner.query("select * from users where username = :username")
                                        .bind("username", username.toLowerCase())
                                        .execute(new BeanHandler<User>(User.class));

            return ret;
        } catch(final SQLException e) {
            LOG.error("Error reading user by name: {}", e.getMessage());
            throw new WicketRuntimeException(e);
        }
    }

    /**
     * Obtain the currently authenticated user.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the currently logged in user object.
     * @return the user.
     */
    public User getUser() {
        return user;
    }

    public interface MetrinkSessionFactory {
        public MetrinkSession create(Request request);
    }

    /**
     * This class is attached as a RequestCycleListener in {@link com.metrink.Application} to repopulate Guice injected
     * fields. Unlike components, Wicket sessions cannot utilize {@link org.apache.wicket.injection.Injector#get()} via
     * a {@link #readObject(ObjectInputStream)} method. The session is deserialized prior to the Wicket application
     * being reattached to the thread.
     * @see http://stackoverflow.com/questions/17254803/apache-wicket-injecting-dependencies-in-session-using-guice
     */
    public static class MetrinkSessionInjectorRequestCycleListener extends AbstractRequestCycleListener {
        @Override
        public void onBeginRequest(final RequestCycle cycle) {
            if (Session.exists()) {
                org.apache.wicket.injection.Injector.get().inject(Session.get());
            }
        }
    }
}
