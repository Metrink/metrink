package com.metrink.gui.login.component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.metric.User;
import com.sop4j.dbutils.QueryRunner;

/**
 * Reset Password {@link Form} for {@link User}s.
 */
public class ResetPasswordForm extends StatelessForm<User> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ResetPasswordForm.class);

    @Inject private transient final QueryRunner queryRunner;
    @Inject private transient final Provider<SimpleEmail> emailProvider;

    /**
     * Initialize the form.
     * @param id the wicket id
     * @param modelFactory the model factory
     * @param queryRunner the dao
     * @param emailProvider the emailProvider
     */
    public ResetPasswordForm(final String id,
                             final DbModelFactory modelFactory,
                             final QueryRunner queryRunner,
                             final Provider<SimpleEmail> emailProvider) {
        super(id);
        this.queryRunner = queryRunner;
        this.emailProvider = emailProvider;

        setDefaultModel(new CompoundPropertyModel<User>(modelFactory.create(User.class, new User())));

        final TextField<String> email = new TextField<String>("username");

        add(email);
    }

    @Override
    protected void onSubmit() {
        final User user = getModelObject();

        try {
            if (queryRunner.read(User.class, user) == null) {
                getSession().error("Sorry! That email isn't registered. Try using the registration form!");
                return;
            }
        } catch (SQLException e) {
            LOG.error("Error reading user {}: {}", user, e.getMessage());
        }

        final StringBuilder message = new StringBuilder("Click the following link to reset your password: \n\n");

        final long time = System.nanoTime();
        try {
            message.append("https://metrink.com/reset-password/")
                   .append(URLEncoder.encode(user.getUsername(), "UTF-8"))
                   .append("/")
                   .append(time)
                   .append("/")
                   .append(user.generateHash(time));
        } catch (final UnsupportedEncodingException e) {
            LOG.error("UnsupportedEncodingException: {}", e.getMessage());
            getSession().error("An error occurred while processing your email address. Please contact support");
            return;
        }

        try {
            final SimpleEmail email = emailProvider.get();

            email.addTo(user.getUsername());
            email.setSubject("Metrink Password Reset");
            email.setMsg(message.toString());
            final String messageId = email.send();

            LOG.info("Sent message {}", messageId);

        } catch (final EmailException e) {
            getSession().error("An error occurred while sending the email. Please contact support.");
            LOG.error("Error sending email: {}", e.getMessage());
            return;
        }

        getSession().success("A link to reset your password has been sent. If it doesn't arrive in a few minutes" +
                " please contact support. Be sure to check your spam folder.");
    }
}