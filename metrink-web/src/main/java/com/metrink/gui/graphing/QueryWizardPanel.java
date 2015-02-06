package com.metrink.gui.graphing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.metrink.grammar.query.QueryFunctionFactory;
import com.metrink.gui.StartEndTimeFormComponent;
import com.metrink.gui.bootstrap.BootstrapAjaxTabbedPanel;
import com.metrink.gui.bootstrap.BootstrapTypeaheadField;
import com.metrink.gui.documentation.GettingStartedPage;
import com.metrink.markdown.Markdown;
import com.metrink.markdown.MarkdownLabel.MarkdownLabelFactory;
import com.metrink.markdown.StringResourceModel;
import com.metrink.metric.MetricId;
import com.metrink.metric.io.MetricMetadata;
import com.metrink.utils.DeserializationUtils;

public class QueryWizardPanel extends Panel {

    private static final long serialVersionUID = 5789748917776799826L;

    private static final Logger LOG = LoggerFactory.getLogger(QueryWizardPanel.class);

    @Inject private transient MetricMetadata metricMetadata;

    @Inject
    public QueryWizardPanel(@Assisted final String id,
                            @Assisted final FormComponent<String> resultComponent,
                            final MetricMetadata metricMetadata,
                            final QueryFunctionFactory queryFunctionFactory) {
        super(id);

        this.metricMetadata = metricMetadata;

        final AbstractTab metricsTab = new AbstractTab(Model.of("Metrics")) {
            private static final long serialVersionUID = 1L;

            @Override
            public WebMarkupContainer getPanel(final String panelId) {
                final Fragment fragment = new Fragment(panelId, "metrics-wizard", QueryWizardPanel.this);
                fragment.add(new MetricsWizardForm("wizard-form", resultComponent));
                return fragment;
            }

        };
        final AbstractTab functionsTab = new AbstractTab(Model.of("Functions")) {
            private static final long serialVersionUID = 1L;

            @Override
            public WebMarkupContainer getPanel(final String panelId) {
                final Fragment fragment = new Fragment(panelId, "functions-wizard", QueryWizardPanel.this);
                fragment.add(new FunctionsWizardForm("wizard-form", resultComponent, queryFunctionFactory.getFunctions()));
                return fragment;
            }

        };

        final BootstrapAjaxTabbedPanel<AbstractTab> tabs = new BootstrapAjaxTabbedPanel<AbstractTab>("tabs",
                Arrays.<AbstractTab> asList(metricsTab, functionsTab)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onAjaxUpdate(final AjaxRequestTarget target) {
                target.appendJavaScript("$('.chzn-select').chosen();");
            }
        };
        add(tabs);
    }

    private static class FunctionsWizardForm extends Form<Void> {
        private static final String COPY = ">| Copy";
        private static final String PIPE = "| Pipe";

        private static final ImmutableList<String> OPERATORS_DESCRIPTIONS = ImmutableList.of(PIPE, COPY);
        private static final Map<String, String> DESCRIPTION_TO_OPERATOR = ImmutableMap.of(
                COPY, ">|",
                PIPE, "|");

        private static final long serialVersionUID = 1L;
        private static final List<String> OPERATORS = OPERATORS_DESCRIPTIONS;

        private String operator = "";
        private String function = "";

        public FunctionsWizardForm(final String id, final FormComponent<String> resultComponent, final List<String> functionList) {
            super(id);

            final DropDownChoice<String> operators = new DropDownChoice<String>("operators",
                    PropertyModel.<String>of(this, "operator"), OPERATORS);

            final DropDownChoice<String> functions = new DropDownChoice<String>("functions",
                    PropertyModel.<String>of(this, "function"), functionList);

            final WebMarkupContainer documentation = new WebMarkupContainer("documentation");

            documentation.add(new MarkdownLabelFactory(new Markdown()).build("content", new StringResourceModel(GettingStartedPage.class, "md/functions/" + functionList.get(0) + ".md")));
            documentation.setOutputMarkupId(true);

            add(operators);
            add(functions);
            add(documentation.setOutputMarkupId(true));
            setOutputMarkupId(true);

            functions.add(new OnChangeAjaxBehavior() {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onUpdate(final AjaxRequestTarget target) {
                    final StringResourceModel model = new StringResourceModel(
                            GettingStartedPage.class, "md/functions/" + function + ".md");

                    try {
                        model.load();
                    } catch (final WicketRuntimeException | IllegalArgumentException e) {
                        LOG.warn("Failed to load markdown resource: {}", e.getMessage());
                        return;
                    }

                    documentation.replace(new MarkdownLabelFactory(new Markdown()).build("content", model).setOutputMarkupId(true));

                    target.add(documentation);
                }
            });

            add(new AjaxButton("insert-button") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

                    final String originalContents = resultComponent.getModelObject();

                    final StringBuffer sb = new StringBuffer();
                    if (!Strings.isNullOrEmpty(originalContents)) {
                        sb.append(originalContents);
                        sb.append(" ");
                    }

                    sb.append(DESCRIPTION_TO_OPERATOR.get(operator));
                    sb.append(" ");
                    sb.append(function);

                    resultComponent.setModelObject(sb.toString());
                    target.add(resultComponent);
                    ModalWindow.closeCurrent(target);
                    target.appendJavaScript("resizeSearchQuery();");
                }
            });
        }
    }

    /**
     * The class the represents the query form.
     *
     * It contains all of the form elements.
     */
    private class MetricsWizardForm extends Form<Void> {
        private static final long serialVersionUID = 1L;

        private final StartEndTimeFormComponent startEndField;
        private final Component deviceField;
        private final Component groupField;
        private final DropDownChoice<String> metricField;
        private final TextField<String> resultField;

        private MetricId metricId;
        private String result; // DO NOT make this final!

        private MetricsWizardForm(final String id, final FormComponent<String> resultComponent) {
            super(id);

            metricId = new MetricId();

            startEndField = new StartEndTimeFormComponent("start-end", Model.<String>of());

            // set the result using the default startEndField
            result = startEndField.getModelObject() + " m(\"*\", \"*\", \"*\")";

            deviceField = new BootstrapTypeaheadField("device", PropertyModel.<String>of(metricId, "device")) {

                private static final long serialVersionUID = 1L;

                @Override
                public Iterator<String> getChoices(final String query) {
                    if (StringUtils.isBlank(query)) {
                        return Collections.emptyIterator();
                    }

                    // this is probably a bit slow
                    final List<String> devices = metricMetadata.readUniqueDevices();
                    final Pattern pattern = query.equals("*") ? Pattern.compile(".*") : Pattern.compile(".*" + query + ".*");

                    // go through the collection and pull out the matches
                    final Collection<String> matches = Collections2.filter(devices, new Predicate<String>() {
                        @Override
                        public boolean apply(final String input) {
                            if (pattern.matcher(input).matches()) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });

                    return matches.iterator();
                }

            };

            final String curGroup = metricId.getGroupName();

            metricField = new DropDownChoice<String>("metric", PropertyModel.<String> of(metricId, "name"),
                    curGroup == null ? Arrays.<String> asList() : metricMetadata.readUniqueMetricNames(curGroup));

            groupField = new GroupDropDownChoice("group", PropertyModel.<String> of(metricId, "groupName"),
                    metricMetadata.readUniqueGroups(), metricField) {

                private static final long serialVersionUID = 1L;

                @Override
                public List<String> getMetricNames(final String groupName) {
                    return metricMetadata.readUniqueMetricNames(groupName);
                }

            };

            resultField = new TextField<String>("result", PropertyModel.<String> of(this, "result"));
            resultField.setOutputMarkupId(true);

            final AjaxButton insertButton = new AjaxButton("insert-button") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                    LOG.debug("Query: {}", result);

                    final String originalContents = resultComponent.getModelObject();

                    final StringBuffer sb = new StringBuffer();

                    if (!Strings.isNullOrEmpty(originalContents)) {
                        final String[] newquery = result.split("[ ]*m\\(", 2);
                        sb.append(newquery[0]);
                        final String[] query = originalContents.split("[ ]*\\|", 2);
                        sb.append(query[0].replaceFirst("^[^(]*m\\(", " m("));
                        sb.append(" ");
                        if (newquery.length == 2) {
                            sb.append("m(");
                            sb.append(newquery[1]);
                        }
                        if (query.length == 2) {
                            sb.append(" |");
                            sb.append(query[1]);
                        }
                        LOG.debug("{} / {} / {}", query[0], result, sb.toString());
                    } else {
                        sb.append(result);
                    }

                    resultComponent.setModelObject(sb.toString());
                    target.add(resultComponent);
                    ModalWindow.closeCurrent(target);
                    target.appendJavaScript("resizeSearchQuery();");
                }

            };

            insertButton.setDefaultFormProcessing(false);

            add(startEndField.add(new UpdateBehavior()));
            add(deviceField.add(new UpdateBehavior()));
            add(groupField.add(new UpdateBehavior()));
            add(metricField.add(new UpdateBehavior()));
            add(resultField);
            add(insertButton);
            this.setOutputMarkupPlaceholderTag(true);
        }

        /**
         * The update behavior that changes the result text.
         */
        private class UpdateBehavior extends OnChangeAjaxBehavior {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                final String startEndValue = (String) startEndField.getDefaultModelObject();
                final String device = (String) deviceField.getDefaultModelObject();
                final String group = (String) groupField.getDefaultModelObject();
                final String metric = (String) metricField.getDefaultModelObject();

                final StringBuilder sb = new StringBuilder();

                sb.append(startEndValue);

                sb.append(" m(\"");
                sb.append(device == null ? "*" : device);
                sb.append("\", \"");
                sb.append(group == null ? "*" : group);
                sb.append("\", \"");
                sb.append(metric == null ? "*" : metric);
                sb.append("\")");

                resultField.setModelObject(sb.toString());

                LOG.debug("SB: {} Result: {}", sb.toString(), result);

                target.add(resultField);
            }
        }
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        DeserializationUtils.readObject(in, this);
    }

    public static interface QueryWizardPanelFactory {
        public QueryWizardPanel create(String id, FormComponent<String> resultComponent);
    }
}
