package com.metrink.gui.stilearn;

import java.util.Iterator;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.gui.MetrinkRoles;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.bootstrap.BootstrapFeedbackPanel;

@AuthorizeInstantiation("USER")
public abstract class StiLearnPage extends StiLearnRootPage {

    private static final Logger LOG = LoggerFactory.getLogger(StiLearnPage.class);

    private static final long serialVersionUID = 1L;

    private final FeedbackPanel feedback;

    public StiLearnPage() {
        feedback = new BootstrapFeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);

        add(new Label("title", getPageTitle()));

        if(LOG.isDebugEnabled()) {
            final FeedbackMessages fms = feedback.getFeedbackMessages();

            final Iterator<FeedbackMessage> it = fms.iterator();

            while(it.hasNext()) {
                final FeedbackMessage m = it.next();

                LOG.debug("({}) {}", m.getLevelAsString(), m.getMessage());
            }
        }

        final MetrinkRoles roles = MetrinkSession.getCurrentRoles();

        add(new WebMarkupContainer("admin-menu") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return roles.contains(MetrinkRoles.ADMIN);
            }
        });

    }

    protected FeedbackPanel getFeedbackPanel() {
        return feedback;
    }
}