package com.metrink.gui.dashboard;

import java.sql.SQLException;
import java.util.regex.Pattern;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Inject;
import com.metrink.dashboard.DashboardBean;
import com.metrink.db.DbModel;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.gui.stilearn.StiLearnPage;
import com.sop4j.dbutils.QueryRunner;

public class DashboardModifyPage extends StiLearnPage {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DashboardModifyPage.class);

    // (?s) toggles single line mode. see Pattern#DOTALL
    private static final Pattern ROW_PATTERN = Pattern.compile("(?s)[ \t]*-[ \t]*row[ \t]*:[ \t]*");
    private static final Pattern GRAPH_PATTERN = Pattern.compile("(?s)[ \t]*-[ \t]*graph[ \t]*:[ \t]*");

    @Inject
    public DashboardModifyPage(final PageParameters pageParameters,
                               final QueryRunner queryRunner,
                               final DbModelFactory dbModelFactory) throws SQLException {

        final StringValue id = pageParameters.get("dashboardId");
        final DbModel<DashboardBean, Integer> dashboardBeanModel = dbModelFactory.create(DashboardBean.class, id.toInt(), "dashboardId");

        if (!pageParameters.get("query").isEmpty()) {
            dashboardBeanModel.getObject().setDefinition("- row:\n  - graph: " + pageParameters.get("query"));
        }

        final Form<DashboardBean> form = new Form<DashboardBean>("form") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit() {
                final DashboardBean dashboardBean = dashboardBeanModel.getObject();

                LOG.debug("Dashboard: {}", dashboardBean);

                dashboardBean.setDefinition(DashboardModifyPage.mungeYaml(dashboardBean.getDefinition()));

                if (!isValidDefinition(dashboardBean.getDefinition())) {
                    return;
                }

                if (dashboardBean.getDashboardId() == null) {
                    try {
                        queryRunner.create(DashboardBean.class, dashboardBean);
                        info("Created dashboard: " + dashboardBean.getDashboardName());
                    } catch (SQLException e) {
                        LOG.error("Error creating dashboard {}: {}", dashboardBean.getDashboardName(), e.getMessage());
                        error("Error creating dashboard: " + dashboardBean.getDashboardName());
                    }
                } else {
                    try {
                        queryRunner.update(DashboardBean.class, dashboardBean);
                        info("Updated dashboard: " + dashboardBean.getDashboardName());
                    } catch (SQLException e) {
                        LOG.error("Error updating dashboard {}: {}", dashboardBean.getDashboardName(), e.getMessage());
                        error("Error updating dashboard: " + dashboardBean.getDashboardName());
                    }
                }

            }
        };

        form.setDefaultModel(new CompoundPropertyModel<DashboardBean>(dashboardBeanModel));

        form.add(new TextField<String>("dashboardName").setRequired(true));
        form.add(new TextArea<String>("definition").setRequired(true));
        form.add(new AjaxButton("cancel", form) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> submitForm) {
                setResponsePage(DashboardListPage.class);
            }

        }.setDefaultFormProcessing(false));

        add(form);
    }

    // package protected so we can test it
    static String mungeYaml(final String yaml) {
        return GRAPH_PATTERN.matcher(ROW_PATTERN.matcher(yaml).replaceAll("- row:")).replaceAll("  - graph: ");
    }

    private boolean isValidDefinition(final String yaml) {
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        final JsonNode tree;

        LOG.debug("YAML: {}", yaml);

        try {
            tree = objectMapper.readTree(yaml);
        } catch (final Exception e) {
            LOG.error("YAML Parse Exception: {}", e.getMessage());
            error("Error parsing dashboard YAML: " + e.getMessage());
            return false;
        }
        if (!tree.isArray()) {
            error("Dashboard is not an array.");
            return false;
        }

        for (final JsonNode row : tree) {
            if (!row.isObject() || !row.has("row")) {
                error("Dashboards should contain a list of rows: " + row);
                return false;
            }

            for (final JsonNode rowEntries : row.get("row")) {
                if (!rowEntries.isObject() || !rowEntries.has("graph")
                        || !JsonNodeType.STRING.equals(rowEntries.get("graph").getNodeType())) {
                    error("Rows should contain a list of graph to query mappings: " + rowEntries);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected String getPageTitle() {
        return "Dashboards";
    }
}
