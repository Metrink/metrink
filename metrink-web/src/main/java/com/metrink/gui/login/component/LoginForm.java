package com.metrink.gui.login.component;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

import com.metrink.gui.MetrinkSession;
import com.metrink.gui.dashboard.DashboardDefaultPage;
import com.metrink.metric.User;

/**
 * Login form.
 */
public class LoginForm extends StatelessForm<User> {
    private static final long serialVersionUID = 1L;

    private final User user;

    /**
     * Initialize the login form.
     * @param id the wicket id
     */
    public LoginForm(final String id) {
        super(id);
        this.user = new User();

        setDefaultModel(new CompoundPropertyModel<User>(user));

        final TextField<String> username = new TextField<String>("username");
        final PasswordTextField password = new PasswordTextField("password");

        // this is only here to make development easier
        if(this.getApplication().getConfigurationType().equals(RuntimeConfigurationType.DEVELOPMENT)) {
            password.setResetPassword(false);
            user.setUsername("metrink");
            user.setPassword("metrink");
        }

        add(username);
        add(password);
    }

    @Override
    protected void onSubmit() {
        final MetrinkSession session = (MetrinkSession)getSession();
        session.replaceSession();

        if (session.login(user.getUsername(), user.getPassword())) {
            continueToOriginalDestination();
            setResponsePage(DashboardDefaultPage.class);
        } else {
            session.error("Invalid credentials");
        }
    }
}