package com.metrink.gui.dashboard;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.stilearn.StiLearnPage;

public class DashboardDefaultPage extends StiLearnPage {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(DashboardDefaultPage.class);
    private static final long serialVersionUID = 1L;

    @Inject
    public DashboardDefaultPage(final DbModelFactory dbModelFactory) {
        final Integer defaultDashboardId = ((MetrinkSession)getSession()).getUser().getDefaultDashboardId();

        // Users might not have a default dashboard ID, either for legacy reasons or because it was unset (somehow)
        if (defaultDashboardId == null) {
            throw new RestartResponseAtInterceptPageException(DashboardListPage.class);
        } else {
            final PageParameters pageParameters = new PageParameters().add("dashboardId", defaultDashboardId);
            throw new RestartResponseAtInterceptPageException(DashboardPage.class, pageParameters);
        }
    }

    @Override
    public String getPageTitle() {
        return "Dashboards";
    }
}
