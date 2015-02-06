package com.metrink.gui.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.gui.JavaScriptHeaderItemUtil;
import com.metrink.gui.bootstrap.BootstrapFeedbackPanel;
import com.metrink.gui.graphing.GraphPage;
import com.metrink.gui.stilearn.StiLearnRootPage;
import com.metrink.metric.User;
import com.sop4j.dbutils.QueryRunner;

/**
 * Reset password page for Metrink.
 */
public class ResetPasswordPage extends StiLearnRootPage {
    private static final Logger LOG = LoggerFactory.getLogger(ResetPasswordPage.class);
    private static final long serialVersionUID = 1L;

    private static final List<ResourceReference> JAVASCRIPT_FILES = Arrays.<ResourceReference>asList(
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.metadata.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.validate.js"));

    private static final String LOGIN_JAVASCRIPT = JavaScriptHeaderItemUtil.forScript(ResetPasswordPage.class, "js/login.js");


    /**
     * Initialize the page.
     * @param pageParameters the page parameters
     * @param modelFactory hibernate model factory
     * @param queryRunner hibernate user dao
     * @param emailProvider the email provider
     * @throws UnsupportedEncodingException if utf8 decoding fails
     */
    @Inject
    public ResetPasswordPage(final PageParameters pageParameters,
                             final DbModelFactory modelFactory,
                             final QueryRunner queryRunner) throws UnsupportedEncodingException {

        final String email = pageParameters.get("email").toString();
        final Long time = pageParameters.get("time").toLong();
        final String hash = pageParameters.get("hash").toString();

        if (email == null || time == null || hash == null) {
            LOG.warn("Attempt to access reset password page without appropriate fields");
            throw new AbortWithHttpErrorCodeException(404, "Not Found");
        }

        final String userHash = getUsersHash(email, time);
        if (!MessageDigest.isEqual(userHash.getBytes("UTF-8"), hash.getBytes("UTF-8"))) {
            LOG.warn("Hashes {} {} did not match", userHash, hash);
            throw new AbortWithHttpErrorCodeException(400, "Bad Request");
        }

        add(new ResetPasswordForm("form", email, modelFactory, queryRunner),
            new BootstrapFeedbackPanel("feedback"));
    }

    /**
     * Get the user's hash from the provided email and time.
     * @param email the email
     * @param time the time
     * @return the correct hash as a hex string
     */
    private String getUsersHash(final String email, final Long time) {
        final User user = new User();
        user.setUsername(email);
        final String userHash = user.generateHash(time);
        return userHash;
    }

    @Override
    protected String getPageTitle() {
        return "Reset Password";
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        for(final ResourceReference js:JAVASCRIPT_FILES) {
            response.render(JavaScriptHeaderItem.forReference(js));
        }
        response.render(JavaScriptHeaderItem.forScript(LOGIN_JAVASCRIPT, "login-js"));
    }

    /**
     * Password reset form.
     */
    private final class ResetPasswordForm extends Form<ResetPasswordForm> {
        private static final long serialVersionUID = 1L;

        private String email;

        private String password;
        @SuppressWarnings("unused")
        private String verifyPassword;

        private QueryRunner queryRunner;

        private ResetPasswordForm(final String id,
                                  final String email,
                                  final DbModelFactory modelFactory,
                                  final QueryRunner queryRunner) {
            super(id);
            this.email = email;
            this.queryRunner = queryRunner;

            setDefaultModel(new CompoundPropertyModel<ResetPasswordForm>(this));

            final PasswordTextField passwordTextField = new PasswordTextField("password");
            final PasswordTextField verifyPasswordTextField = new PasswordTextField("verifyPassword");

            add(passwordTextField, verifyPasswordTextField);
            add(new EqualPasswordInputValidator(passwordTextField, verifyPasswordTextField));
        }

        @Override
        protected void onSubmit() {
            final User detachedUser = new User();
            detachedUser.setUsername(email);

            try {
                final User user = queryRunner.read(User.class, detachedUser);

                if (user == null) {
                    getSession().error("Email address provided is not found");
                    return;
                }

                user.setPassword(password);

                queryRunner.update(User.class, user);
                getSession().success("Password reset! Please login to confirm successful reset.");
            } catch (SQLException e) {
                getSession().error("Failed to update password");
            }

            setResponsePage(GraphPage.class);
        }
    }
}