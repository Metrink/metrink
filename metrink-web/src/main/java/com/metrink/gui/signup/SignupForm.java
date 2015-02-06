package com.metrink.gui.signup;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.metrink.dashboard.DashboardBean;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.documentation.GettingStartedPage;
import com.metrink.metric.User;
import com.metrink.utils.DeserializationUtils;
import com.sop4j.dbutils.QueryRunner;

/**
 * Registration {@link Form} for {@link User}s.
 */
public class SignupForm extends StatelessForm<User> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SignupForm.class);

    /**
     * Verify password field is defined here rather than within the {@link User} class. This avoids cluttering the
     * shared object with registration specific business logic. Despite being suppressed as unused, it is actually
     * used by the {@link EqualPasswordInputValidator}.
     */
    @SuppressWarnings("unused")
    private String verifyPassword;

    private QueryRunner queryRunner;
    private final Provider<SimpleEmail> emailProvider;
    private final boolean isNewUser;

    public SignupForm(final String id,
                      final User user,
                      final DbModelFactory modelFactory,
                      final QueryRunner queryRunner,
                      final Provider<SimpleEmail> emailProvider) {
        super(id);
        this.queryRunner = queryRunner;
        this.emailProvider = emailProvider;

        if(user == null) {
            this.isNewUser = true;
            setDefaultModel(new CompoundPropertyModel<User>(modelFactory.create(User.class, new User())));
        } else {
            this.isNewUser = false;
            setDefaultModel(new CompoundPropertyModel<User>(modelFactory.create(User.class, user)));
        }

        final TextField<String> name = new TextField<String>("name");
        final TextField<String> email = new TextField<String>("username") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return isNewUser;
            }
        };

        final DropDownChoice<String> timezone =
                (DropDownChoice<String>) new DropDownChoice<String>("timezone", new ArrayList<String>(User.TIMEZONES)).setRequired(true);

        final PasswordTextField password = new PasswordTextField("password") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return isNewUser;
            }
        };

        final PasswordTextField verifyPassword = new PasswordTextField("verifyPassword",
                new PropertyModel<String>(this, "verifyPassword")) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return isNewUser;
            }
        };

        add(name, email, timezone, password, verifyPassword);
        add(new EqualPasswordInputValidator(password, verifyPassword));
    }

    @Override
    protected void onSubmit() {
        final User user = getModelObject();

        if(isNewUser) {
            final DateTime now = new DateTime(DateTimeZone.UTC);

            user.setUsername(user.getUsername().toLowerCase()); // always lowercase the username
            user.setCreated(new Date(now.getMillis()));
        }

        try {
            if (isNewUser && queryRunner.read(User.class, user) != null) {
                getSession().error("Sorry! That email address is taken. Try requesting a password reset.");
                return;
            }
        } catch (final SQLException e) {
            LOG.error("Error reading user: {}", e.getMessage());
            throw new WicketRuntimeException(e);
        }

        if(isNewUser) {
            DashboardBean defaultDashboard;
            try {
                // initialize the default dashboard for the metrink user
                InputStream definition = getClass().getClassLoader().getResourceAsStream("default_dashboard.yml");

                defaultDashboard = new DashboardBean();
                defaultDashboard.setDashboardName("Default Dashboard");
                defaultDashboard.setDefinition(IOUtils.toString(definition));

                queryRunner.create(DashboardBean.class, defaultDashboard);
            } catch(final SQLException | IOException e) {
                getSession().error("An unexpected error occurred during initialization");
                LOG.error("Failed to create default dashboard: {}", e.getMessage());
                return;
            }

            try {
                //
                // FIXME: this isn't correct... we need the ID after creation
                defaultDashboard = queryRunner.read(DashboardBean.class, defaultDashboard);
            } catch(final SQLException e) {
                getSession().error("An unexpected error occurred during initialization");
                LOG.error("Failed to read owner: {}", e.getMessage());
                return;
            }

            try {
                // set to the fresh owner
                user.setDefaultDashboardId(defaultDashboard.getDashboardId());
                queryRunner.create(User.class, user);
            } catch(final SQLException e) {
                getSession().error("An unexpected error occurred during registration");
                LOG.error("Failed to persist user: {}", user, e);
                return;
            }

            // by here the user is all signed up and in the DB, so let's alert sales
            try {
                final StringBuilder messageBody = new StringBuilder("A new user signed up: \n");

                messageBody.append(user.toString());

                final SimpleEmail email = emailProvider.get();
                email.setSubject("[METRINK] New User Signed Up");
                email.addTo("sales@metrink.com");
                email.setMsg(messageBody.toString());

                final String messageId = email.send();

                LOG.info("New user email sent: {}", messageId);
            } catch (final EmailException e) {
                LOG.error("Error sending new user email: {}", e.getMessage());
            }
        } else {
            boolean worked = false;

            try {
                worked = queryRunner.update(User.class, user) > 0;
            } catch(final SQLException e) {
                LOG.error("Error calling update: {}", e.getMessage());
            }

            if(!worked) {
                getSession().error("An unexpected error occurred while updating your profile");
                LOG.error("Failed to update user: {}", user);
                return;
            }
        }

        final MetrinkSession metrinkSession = (MetrinkSession)getSession();
        metrinkSession.replaceSession();

        if(!isNewUser) {
            getSession().success("Profile updated");
            LOG.debug("Updated profile for user: {}", user);
            setResponsePage(EditProfilePage.class);
        } else if (metrinkSession.login(user.getUsername(), user.getPassword())) {
            getSession().success("Congratulations and Welcome to Metrink!");
            LOG.debug("Registered user: {}", user);
            setResponsePage(GettingStartedPage.class);
        } else {
            metrinkSession.error("Failed to sign-in after registration");
            LOG.error("Although we succeeded in persisting the user, automatic sign-in failed");
            return;
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        DeserializationUtils.readObject(in, this);
    }
}