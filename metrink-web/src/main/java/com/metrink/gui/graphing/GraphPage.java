package com.metrink.gui.graphing;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.graph.GraphObject;
import com.metrink.grammar.graph.GraphQuery;
import com.metrink.grammar.graph.GraphQueryParser;
import com.metrink.gui.dashboard.DashboardModifyPage;
import com.metrink.gui.graphing.QueryWizardPanel.QueryWizardPanelFactory;
import com.metrink.gui.stilearn.StiLearnPage;

public class GraphPage extends StiLearnPage {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(GraphPage.class);

    private static final JavaScriptResourceReference WRENCH_POPUP =
            new JavaScriptResourceReference(GraphPage.class, "js/trial_highlight_wrench.js");


    private transient final GraphQueryParser queryFactory;
    private transient final QueryWizardPanelFactory queryWizardPanelFactory;

    private final String searchQuery;
    private TextField<String> searchQueryField;

    @Inject
    public GraphPage(final GraphQueryParser queryFactory,
                     final QueryWizardPanelFactory queryWizardPanelFactory,
                     final PageParameters params) {


        this.queryFactory = queryFactory;
        this.queryWizardPanelFactory = queryWizardPanelFactory;

        searchQuery = params.get("query").toString("");

        // setup the form and modal window
        setupForm();

        LOG.debug("QUERY: {}", searchQuery);

        add(new GraphPanel("graph-panel", createGraphObject()));
    }

    protected void setupForm() {
        final StatelessForm<Void> form = new StatelessForm<Void>("form") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {

                if(LOG.isDebugEnabled()) {
                    LOG.debug("QUERY: {}", searchQuery);
                }

                final PageParameters params = new PageParameters();

                params.add("query", searchQuery);

                this.setResponsePage(GraphPage.class, params);
            }

        };

        searchQueryField = new TextField<String>("search-query", PropertyModel.<String>of(this, "searchQuery"));
        searchQueryField.setRequired(true);
        searchQueryField.setOutputMarkupId(true);
        form.add(searchQueryField);

        form.add(new Button("graph-button", Model.of("Graph")));

        // move this into QueryWizardPanel so it encapsulates everything?
        final ModalWindow queryWizardWindow = new ModalWindow("query-wizard") {
            private static final long serialVersionUID = 1L;

            @Override
            protected CharSequence getShowJavaScript() {
                return "window.setTimeout(function(){\n" + "  Wicket.Window.create(settings).show();\n"
                        + "$('.chzn-select').chosen(); }, 0);\n";
            }
        };

        queryWizardWindow.setContent(queryWizardPanelFactory.create(queryWizardWindow.getContentId(), searchQueryField));
        queryWizardWindow.setTitle("Query Wizard");
        queryWizardWindow.setResizable(false);

        // is this the only way to set these?
        queryWizardWindow.setInitialWidth(700);
        queryWizardWindow.setInitialHeight(600);

        form.add(new AjaxLink<Void>("query-wizard-link") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                queryWizardWindow.show(target);
            }

        });

        form.add(new AjaxLink<Void>("save-dashboard") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(final AjaxRequestTarget target) {
                final String query = searchQueryField.getValue();
                setResponsePage(DashboardModifyPage.class, new PageParameters().add("query", query));
            }
        });
        add(queryWizardWindow);
        add(form);
    }

    private GraphObject createGraphObject() {
        if(StringUtils.isBlank(searchQuery)) {
            return null;
        }

        try {
            final GraphQuery plan = queryFactory.createGraphQuery(searchQuery);

            return plan.execute();
        } catch(final MetrinkParseException e) {
            LOG.error("There was an error parsing the search string: {}", e.getMessage());
            LOG.debug("MetrinkParseException: ", e);
            getSession().error(e.getMessage());
        }

        return null;
    }

    @Override
    protected String getPageTitle() {
        return "Graphing";
    }

}
