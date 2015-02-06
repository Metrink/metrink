package com.metrink.gui.dashboard;

import java.sql.SQLException;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.metrink.dashboard.DashboardBean;
import com.metrink.db.DbModel;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.graph.GraphObject;
import com.metrink.grammar.graph.GraphQuery;
import com.metrink.grammar.graph.GraphQueryParser;
import com.metrink.gui.graphing.GraphPanel;
import com.metrink.gui.stilearn.StiLearnPage;

public class DashboardPage extends StiLearnPage {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardPage.class);
    private static final long serialVersionUID = 1L;

    private final GraphQueryParser graphQueryParser;

    @Inject
    public DashboardPage(final GraphQueryParser graphQueryParser,
                         final PageParameters pageParameters,
                         final DbModelFactory dbModelFactory) {
        this.graphQueryParser = graphQueryParser;

        final int id = pageParameters.get("dashboardId").toInt();
        DbModel<DashboardBean, Integer> model = null;

        try {
            model = dbModelFactory.create(DashboardBean.class, id, "dashboardId");
        } catch(SQLException e) {
            throw new WicketRuntimeException(e);
        }

        add(new BookmarkablePageLink<Page>("list", DashboardListPage.class));
        add(new BookmarkablePageLink<Page>("edit", DashboardModifyPage.class, pageParameters));
        add(new Label("dashboardName", new PropertyModel<String>(model, "dashboardName")));

        final String definition = model.getObject().getDefinition();
        add(new ListView<List<String>>("rows", new ListModel<List<String>>(getGrid(definition))) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(final ListItem<List<String>> row) {

                final int cells = row.getModelObject().size();

                // make sure we didn't divide by zero here
                if(cells == 0) {
                    LOG.warn("Found a row with no graph objects");
                    return;
                }

                final int span = 12 / cells;

                row.add(new ListView<String>("cell", new ListModel<String>(row.getModelObject())) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected void populateItem(final ListItem<String> cell) {
                        final String suffix = "-" + row.getIndex() + "-" + cell.getIndex();
                        final GraphObject query = createGraphFromQuery(cell.getModelObject());
                        final GraphPanel graphPanel = new GraphPanel("graph-panel", query, suffix);
                        graphPanel.setHeight(225);
                        cell.add(new Label("definition", cell.getDefaultModel()));
                        cell.add(graphPanel);
                        cell.add(new AttributeModifier("class", "span" + Math.max(1, span)));
                    }
                });
            }
        });
    }

    private JsonNode getDefinition(final String yaml) {
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        try {
            return objectMapper.readTree(yaml);
        } catch (final Exception e) {
            LOG.error("YAML Parse Exception: {}", e.getMessage());
            LOG.error(yaml);
            throw new IllegalStateException("Error parsing YAML: {}" + e.getMessage());
        }
    }

    private List<List<String>> getGrid(final String yaml) {
        final List<List<String>> results = Lists.newArrayList();
        for (final JsonNode row : getDefinition(yaml)) {
            if (!row.has("row")) {
                continue;
            }

            final List<String> rowResult = Lists.newArrayList();
            for (final JsonNode cell : row.get("row")) {
                if (!cell.has("graph")) {
                    continue;
                }

                rowResult.add(cell.get("graph").asText());
            }
            results.add(rowResult);
        }
        return results;
    }

    /**
     * This might be a terribly expensive function.
     */
    private GraphObject createGraphFromQuery(final String query) {
        try {
            final GraphQuery plan = graphQueryParser.createGraphQuery(query);
            final GraphObject graphObject = plan.execute();

            LOG.debug("Graphing: {}", query);

            return graphObject;
        } catch(final MetrinkParseException e) {
            LOG.error("Error parsing query: {}", e.getMessage());
            getSession().error("Error parsing query: " + e.getMessage());
        }

        return null;
    }

    @Override
    protected String getPageTitle() {
        return "Dashboard";
    }
}
