package com.metrink.gui.login;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.gui.stilearn.StiLearnRootPage;

public class LogoutPage extends StiLearnRootPage {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(LogoutPage.class);
    private static final long serialVersionUID = 1L;

    @Inject
    public LogoutPage(final PageParameters params) {
        getSession().invalidate();
        setResponsePage(LoginPage.class);
    }

    @Override
    protected String getPageTitle() {
        return "Logout";
    }
}
