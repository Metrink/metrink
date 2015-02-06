package com.metrink.gui.login;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.mail.SimpleEmail;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.gui.JavaScriptHeaderItemUtil;
import com.metrink.gui.bootstrap.BootstrapFeedbackPanel;
import com.metrink.gui.login.component.LoginForm;
import com.metrink.gui.login.component.ResetPasswordForm;
import com.metrink.gui.stilearn.StiLearnRootPage;
import com.sop4j.dbutils.QueryRunner;

/**
 * Login and registration page for Metrink.
 */
public class LoginPage extends StiLearnRootPage {
    //private static final Logger LOG = LoggerFactory.getLogger(LoginPage.class);
    private static final long serialVersionUID = 1L;

    private static final List<ResourceReference> JAVASCRIPT_FILES = Arrays.<ResourceReference>asList(
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.metadata.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.validate.js"));

    private static final String LOGIN_JAVASCRIPT = JavaScriptHeaderItemUtil.forScript(LoginPage.class, "js/login.js");

    /**
     * Initialize the page.
     * @param modelFactory hibernate model factory
     * @param queryRunner hibernate user dao
     * @param emailProvider the email provider
     */
    @Inject
    public LoginPage(final DbModelFactory modelFactory,
                     final QueryRunner queryRunner,
                     final Provider<SimpleEmail> emailProvider) {

        add(new BootstrapFeedbackPanel("feedback"),
            new LoginForm("login-form"),
            new ResetPasswordForm("reset-password-form", modelFactory, queryRunner, emailProvider));
    }

    @Override
    protected String getPageTitle() {
        return "Login";
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
}
