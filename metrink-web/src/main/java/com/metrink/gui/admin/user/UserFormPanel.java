package com.metrink.gui.admin.user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.dashboard.DashboardBean;
import com.metrink.gui.JavaScriptHeaderItemUtil;
import com.metrink.gui.MetrinkRoles;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.login.LoginPage;
import com.metrink.gui.stilearn.StiLearnRootPage;
import com.metrink.metric.User;
import com.metrink.utils.DeserializationUtils;
import com.sop4j.dbutils.EntityUtils;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanListHandler;
import com.sop4j.dbutils.handlers.ScalarHandler;

/**
 * Registration {@link Form} for {@link User}s.
 */
public class UserFormPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(UserFormPanel.class);

    private static final List<ResourceReference> JAVASCRIPT_FILES = Arrays.<ResourceReference>asList(
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.metadata.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.validate.js"));

    private static final String LOGIN_JAVASCRIPT = JavaScriptHeaderItemUtil.forScript(LoginPage.class, "js/login.js");

    /**
     * Verify password field is defined here rather than within the {@link User} class. This avoids cluttering the
     * shared object with registration specific business logic. Despite being suppressed as unused, it is actually
     * used by the {@link EqualPasswordInputValidator}.
     */
    @SuppressWarnings("unused")
    private String verifyPassword;

    private QueryRunner queryRunner;

    private IModel<User> userModel;

    private final User currentUser;
    private Form<User> form;
    private Button submitButton;

    public UserFormPanel(final String id,
                         final QueryRunner queryRunner) {
        super(id);
        this.queryRunner = queryRunner;
        this.currentUser = MetrinkSession.getCurrentUser();
        this.userModel = Model.of(new User());

        final TextField<String> name = new TextField<String>("name");

        final TextField<String> email = new TextField<String>("username");

        final DropDownChoice<String> roles = new DropDownChoice<>("roles", PropertyModel.<String>of(userModel, "roles"), Arrays.asList(MetrinkRoles.USER, MetrinkRoles.ADMIN));

        final DropDownChoice<String> timezone =
                (DropDownChoice<String>) new DropDownChoice<String>("timezone", new ArrayList<String>(User.TIMEZONES)).setRequired(true);

        final PasswordTextField password = (PasswordTextField) new PasswordTextField("password").setRequired(false);

        final PasswordTextField verifyPassword =
                (PasswordTextField) new PasswordTextField("verifyPassword", new PropertyModel<String>(this, "verifyPassword")).setRequired(false);

        submitButton = new Button("submit-button");

        form = new Form<User>("user-form", userModel) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                final User user = getModelObject();

                // check to see if we have a new user or not
                if(user.getUserId() == null) {
                    final DateTime now = new DateTime(DateTimeZone.UTC);

                    user.setUsername(user.getUsername().toLowerCase()); // always lowercase the username
                    user.setCreated(new Date(now.getMillis()));

                    // make sure they entered a password
                    if(StringUtils.isBlank(user.getPassword())) {
                        getSession().error("You must enter a password for new users!");
                        return;
                    }

                    Long userFound;

                    try {
                        // make sure we're not duplicating users
                        userFound = UserFormPanel.this.queryRunner.query("select count(*) from " + EntityUtils.getTableName(User.class) + " where username = :username")
                                                          .bind("username", user.getUsername())
                                                          .execute(new ScalarHandler<Long>());
                    } catch (final SQLException e) {
                        LOG.error("Error getting count of users: {}", e.getMessage());
                        throw new WicketRuntimeException(e);
                    }


                    try {
                        List<DashboardBean> defaultDashboardIds =
                                UserFormPanel.this.queryRunner.query("select * from " + EntityUtils.getTableName(DashboardBean.class) + " order by created desc")
                                                              .execute(new BeanListHandler<DashboardBean>(DashboardBean.class));

                        if (defaultDashboardIds.size() > 0) {
                            user.setDefaultDashboardId(defaultDashboardIds.get(0).getDashboardId());
                        }

                    } catch (final SQLException e) {
                        LOG.error("Error getting count of users: {}", e.getMessage());
                        throw new WicketRuntimeException(e);
                    }

                    if (userFound > 0) {
                        getSession().error("Sorry! That email address is taken.");
                        return;
                    }

                    try {
                        queryRunner.create(User.class, user);
                    } catch(SQLException e) {
                        getSession().error("An unexpected error occurred while creating user");
                        LOG.error("Failed to create user {}: {}", user, e.getMessage());
                        return;
                    }

                    getSession().success(user.getName() + " created successfully!");
                    LOG.debug("{} created user: {}", currentUser.getUsername(), user);
                } else {
                    // need to make sure we don't blow away the password if it's blank
                    if(StringUtils.isBlank(user.getPassword())) {
                        try {
                            final User dbUser = queryRunner.read(User.class, user);
                                                           //.bind(user.getIdColumn(), user.getId())
                                                           //.read();

                            user.setPassword(dbUser.getPassword());
                        } catch(SQLException e) {
                            getSession().error("An unexpected error occurred while reading user");
                            LOG.error("Failed to read user {}: {}", user, e.getMessage());
                            return;
                        }
                    }

                    try {
                        queryRunner.update(User.class, user);
                    } catch(SQLException e) {
                        getSession().error("An unexpected error occurred while updating user");
                        LOG.error("Failed to update user {}: {}", user, e.getMessage());
                        return;
                    }

                    getSession().success(user.getName() + " updated successfully!");
                    LOG.debug("{} updated user: {}", currentUser.getUsername(), user);
                }
            }
        };

        form.add(new AjaxButton("cancel-button", form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> submitForm) {
                LOG.debug("Clearing form");

                // reset the model
                userModel.setObject(new User());

                form.modelChanged();
                form.clearInput();

                target.add(form);

                // update the button
                submitButton.setModel(Model.of("New User"));
                target.add(submitButton);
            }
        }.setDefaultFormProcessing(false));

        form.setDefaultModel(new CompoundPropertyModel<User>(userModel));
        form.add(name, email, roles, timezone, password, verifyPassword, submitButton);
        form.add(new EqualPasswordInputValidator(password, verifyPassword) {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(final Form<?> form) {
                final String pass1 = getDependentFormComponents()[0].getInput();
                final String pass2 = getDependentFormComponents()[1].getInput();

                if(pass1.isEmpty() && pass2.isEmpty()) {
                    return;
                } else if(!pass1.equals(pass2)){
                        error(getDependentFormComponents()[1]);
                }
            }

        });

        add(form);
    }

    public void setForm(final User user, final AjaxRequestTarget target) {
        // update the model with the new bean
        userModel.setObject(user);

        // add the form to the target
        target.add(form);

        // update the button
        submitButton.setModel(Model.of("Update User"));
        target.add(submitButton);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        for(final ResourceReference js:JAVASCRIPT_FILES) {
            response.render(JavaScriptHeaderItem.forReference(js));
        }
        response.render(JavaScriptHeaderItem.forScript(LOGIN_JAVASCRIPT, "login-js"));

        response.render(CssHeaderItem.forCSS(".box-body a { color: #0088cc; }", "link-fix"));
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        DeserializationUtils.readObject(in, this);
    }
}