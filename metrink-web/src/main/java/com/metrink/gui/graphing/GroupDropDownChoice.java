package com.metrink.gui.graphing;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GroupDropDownChoice extends DropDownChoice<String> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(GroupDropDownChoice.class);

    public GroupDropDownChoice(String id,
                               IModel<String> model,
                               List<String> choices,
                               final DropDownChoice<String> metricChoice) {
        super(id, model, choices);

        this.setOutputMarkupId(true);
        metricChoice.setOutputMarkupId(true);

        this.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                final String groupName = GroupDropDownChoice.this.getDefaultModelObjectAsString();

                LOG.debug("New group name: {}", groupName);

                metricChoice.setChoices(getMetricNames(groupName));

                target.add(metricChoice);

                //
                // for some reason $('id').trigger('liszt:updated'); does not work (http://harvesthq.github.io/chosen/)
                // so instead we remove the old div and simply make a new one
                //
                final StringBuffer js = new StringBuffer("$('#");

                js.append(metricChoice.getMarkupId());
                js.append("_chzn').remove();");

                js.append(" $('#");
                js.append(metricChoice.getMarkupId());
                js.append("').chosen();");

                target.appendJavaScript(js.toString());
            }

        });
    }

    public abstract List<String> getMetricNames(String groupName);
}
