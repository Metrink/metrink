package com.metrink.gui.admin;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.gui.admin.user.UserDataProvider.UserDataProviderFactory;
import com.metrink.gui.admin.user.UserFormPanel;
import com.metrink.gui.admin.user.UserTablePanel;
import com.metrink.gui.stilearn.StiLearnPage;
import com.metrink.gui.stilearn.StiLearnRootPage;
import com.sop4j.dbutils.QueryRunner;

/**
 * Login and registration page for Metrink.
 */
public class AdminPage extends StiLearnPage {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(AdminPage.class);
    private static final long serialVersionUID = 1L;

    private static final List<ResourceReference> JAVASCRIPT_FILES = Arrays.<ResourceReference>asList(
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.metadata.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/validate/jquery.validate.js"));

    @Inject
    public AdminPage(final PageParameters params,
                     final UserDataProviderFactory userDataProviderFactory,
                     final QueryRunner queryRunner) {

        final UserFormPanel userFormPanel = new UserFormPanel("user-form", queryRunner);
        final UserTablePanel userTablePanel = new UserTablePanel("user-panel", params, userDataProviderFactory, userFormPanel);

        add(userFormPanel, userTablePanel);
    }

    @Override
    protected String getPageTitle() {
        return "Administrator Settings";
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        for(final ResourceReference js:JAVASCRIPT_FILES) {
            response.render(JavaScriptHeaderItem.forReference(js));
        }

        response.render(CssHeaderItem.forCSS(".box-body a { color: #0088cc; }", "link-fix"));
    }
}
