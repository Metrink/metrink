package com.metrink.gui;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.gui.graphing.DateTimePickerField;

/**
 * A "FormComponent" that provides markup and functionality for selecting times
 */
public class StartEndTimeFormComponent extends FormComponentPanel<String> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(StartEndTimeFormComponent.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm aa");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm aa");
    private static final List<String> TIME_UNITS = Arrays.asList("m", "h", "d", "w");

    private static final JavaScriptResourceReference DATE_PICKER_JS =
            new JavaScriptResourceReference(StartEndTimeFormComponent.class, "js/bootstrap-datetimepicker.min.js");
    private static final CssResourceReference DATE_PICKER_CSS =
            new CssResourceReference(StartEndTimeFormComponent.class, "css/bootstrap-datetimepicker.min.css");

    private final CheckBox relativeTimeCheckBox;
    private boolean relativeTime = false;

    private final WebMarkupContainer absoluteContainer;
    private final WebMarkupContainer relativeContainer;

    private final DateTimePickerField startField;
    private final DateTimePickerField endField;

    private final NumberTextField<Integer> countField; // number of hours, minutes, days, etc
    private final DropDownChoice<String> unitField; // the time unit: minutes, hours, days, etc

    /**
     * Creates the panel with the given ID and associated model.
     * @param id the id of the panel.
     * @param model the model for this form component.
     */
    public StartEndTimeFormComponent(final String id, final IModel<String> model) {
        super(id, model);

        // Setup the fragments and link their visibility to relativeTime
        absoluteContainer = new WebMarkupContainer("absolute-time-container") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return !relativeTime;
            }
        };

        relativeContainer = new WebMarkupContainer("relative-time-container") {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return relativeTime;
            }
        };

        absoluteContainer.setOutputMarkupPlaceholderTag(true);
        relativeContainer.setOutputMarkupPlaceholderTag(true);
        this.setOutputMarkupPlaceholderTag(true);

        // add the fields for start & end
        startField = new DateTimePickerField("start", Model.of(DateTime.now().minusMinutes(30)));
        endField = new DateTimePickerField("end", Model.of(DateTime.now()));
        startField.add(new BubblingOnChangeBehavior());
        endField.add(new BubblingOnChangeBehavior());
        absoluteContainer.add(startField);
        absoluteContainer.add(endField);


        // add the count & unit fields
        countField = new NumberTextField<Integer>("count", Model.<Integer>of(-30));
        unitField = new DropDownChoice<String>("unit", Model.of("m"), TIME_UNITS, new TimeUnitRenderer());
        countField.add(new BubblingOnChangeBehavior()).setOutputMarkupPlaceholderTag(true);
        unitField.add(new BubblingOnChangeBehavior()).setOutputMarkupPlaceholderTag(true);
        relativeContainer.add(countField);
        relativeContainer.add(unitField);

        // setup the ajax check box to change the visibility
/*
        relativeTimeCheckBox = new AjaxCheckBox("relative-time", Model.of(relativeTime)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                relativeTime = relativeTime ? false : true;

                LOG.debug("Setting default model object");
                StartEndTimeFormComponent.this.setDefaultModelObject(inputToString());

                target.add(absoluteContainer);
                target.add(relativeContainer);
            }

        };
*/
        relativeTimeCheckBox = new CheckBox("relative-time", Model.of(relativeTime));

        relativeTimeCheckBox.add(new BubblingOnChangeBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                relativeTime = relativeTime ? false : true;

                LOG.debug("Setting default model object");
                StartEndTimeFormComponent.this.setDefaultModelObject(inputToString());

                target.add(absoluteContainer);
                target.add(relativeContainer);

                target.appendJavaScript("configure_pickers();");
            }

        });

        // add the checkbox & fragments to the panel
        add(relativeTimeCheckBox);
        add(absoluteContainer);
        add(relativeContainer);

        // update the model so it has the starting string
        model.setObject(inputToString());
    }

    protected String inputToString() {
        if(relativeTime){
            final Integer count = countField.getModelObject();
            final String unit = unitField.getModelObject();

            return count + unit;
        } else {
            final DateTime startDate = startField.getModelObject();
            final DateTime endDate = endField.getModelObject();

            final StringBuilder sb = new StringBuilder();

            sb.append(DATE_FORMATTER.print(startDate));
            sb.append(" to ");

            // we only need the time if it's the same day
            if(startDate.getDayOfYear() == endDate.getDayOfYear() &&
               startDate.getYear() == endDate.getYear()) {
                sb.append(TIME_FORMATTER.print(endDate));
            } else {
                sb.append(DATE_FORMATTER.print(endDate));
            }

            return sb.toString();
        }
    }

    @Override
    protected void convertInput() {
        LOG.debug("Converting input...");
        this.setConvertedInput(inputToString());
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        // include chosen & bootstrap
        response.render(JavaScriptReferenceHeaderItem.forReference(DATE_PICKER_JS));
        response.render(CssReferenceHeaderItem.forReference(DATE_PICKER_CSS));
    }

    /**
     * Class that allows an AJAX request to bubble up to other components.
     */
    private static class BubblingOnChangeBehavior extends OnChangeAjaxBehavior {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {
            // intentionally left blank, we just need to attach an AjaxBehavior so it bubbles up
            // see method below
        }

        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);

            attributes.setEventPropagation(EventPropagation.BUBBLE);
        }
    }

    /**
     * Renderer for the time units.
     */
    private static class TimeUnitRenderer implements IChoiceRenderer<String> {

        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final String unit) {
            switch(unit) {
            case "m":
                return "Minutes";
            case "h":
                return "Hours";
            case "d":
                return "Days";
            case "w":
                return "Weeks";
            }

            return null;
        }

        @Override
        public String getIdValue(final String object, final int index) {
            return TIME_UNITS.get(index);
        }
    }

}
