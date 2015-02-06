package com.metrink.gui.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;


// https://cwiki.apache.org/confluence/display/WICKET/CSS-enabled+feedback+panel
public class BootstrapFeedbackPanel extends FeedbackPanel {

    private static final long serialVersionUID = 1L;

    public BootstrapFeedbackPanel(final String id, final IFeedbackMessageFilter filter) {
        super(id, filter);
        unstyleList();
    }

    public BootstrapFeedbackPanel(final String id) {
        super(id);
        unstyleList();
    }

    private void unstyleList() {
        get("feedbackul").add(new AttributeAppender("class", " unstyled"));
    }

    @Override
    protected Component newMessageDisplayComponent(final String id,
            final FeedbackMessage message) {
        final Component newMessageDisplayComponent = super
                .newMessageDisplayComponent(id, message);

        /*
         * CSS class resulting: feedbackUNDEFINED feedbackDEBUG feedbackINFO
         * feedbackWARNING feedbackERROR feedbackFATAL
         */
        final String clazz;
        switch(message.getLevelAsString()) {
        case "ERROR":
            clazz = "alert-error";
            break;
        case "WARNING":
            clazz = "";
            break;
        default:
            clazz = "alert-success";
        }

        newMessageDisplayComponent
                .add(new AttributeAppender("class", new Model<String>("span12 alert " + clazz), " "));
        return newMessageDisplayComponent;
    }
}