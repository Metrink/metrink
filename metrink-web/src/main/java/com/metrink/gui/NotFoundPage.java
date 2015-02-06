package com.metrink.gui;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.gui.stilearn.StiLearnRootPage;
import com.google.inject.Inject;

public class NotFoundPage extends StiLearnRootPage {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(NotFoundPage.class);

    @Inject
    public NotFoundPage() {
    }

    @Override
    protected void configureResponse(final WebResponse response) {
        super.configureResponse(response);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }

    @Override
    protected String getPageTitle() {
        return "Page Not Found";
    }

}