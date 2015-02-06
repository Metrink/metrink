package com.metrink.gui.documentation;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.google.inject.Inject;
import com.metrink.gui.alert.AlertPage;
import com.metrink.gui.graphing.GraphPage;
import com.metrink.gui.stilearn.StiLearnPage;

public class GettingStartedPage extends StiLearnPage {
    private static final long serialVersionUID = 1L;
    //private static final Logger LOG = LoggerFactory.getLogger(GettingStartedPage.class);

    @Inject
    public GettingStartedPage() {
        add(new BookmarkablePageLink<GraphPage>("graphingLink", GraphPage.class));
        add(new BookmarkablePageLink<AlertPage>("alertingLink", AlertPage.class));
    }

    @Override
    protected String getPageTitle() {
        return "Getting Started";
    }
}
