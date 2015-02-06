package com.metrink.gui.alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.metrink.alert.ActionBean;
import com.metrink.alert.AlertBean;
import com.metrink.db.DbDataProvider.DbDataProviderFactory;
import com.metrink.db.DbModel;
import com.metrink.db.DbModel.DbModelFactory;
import com.metrink.grammar.MetrinkParseException;
import com.metrink.grammar.alert.AlertQuery;
import com.metrink.grammar.alert.AlertQueryParser;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.component.BootstrapAjaxDataTable;
import com.metrink.gui.stilearn.StiLearnPage;
import com.sop4j.dbutils.EntityUtils;
import com.sop4j.dbutils.QueryRunner;
import com.sop4j.dbutils.handlers.BeanHandler;

public class AlertPage extends StiLearnPage {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AlertPage.class);

    private static final SortParam<String> DEFAULT_SORT = new SortParam<String>("alertQuery", true);

    private transient QueryRunner queryRunner;
    private transient DbDataProviderFactory dataProviderFactory;
    private transient AlertQueryParser alertQueryParser;

    private final DbModel<AlertBean, Integer> alertBeanModel;
    private Form<AlertBean> form;
    private Button alertButton;

    @Inject
    public AlertPage(final QueryRunner queryRunner,
                     final DbModelFactory hibernateModelFactory,
                     final AlertQueryParser alertQueryParser,
                     final DbDataProviderFactory dataProviderFactory) {

        this.queryRunner = queryRunner;
        this.dataProviderFactory = dataProviderFactory;
        this.alertQueryParser = alertQueryParser;

        this.alertBeanModel = hibernateModelFactory.create(AlertBean.class, new AlertBean());

        setupForm();
        setupTable();
    }

    protected void setupTable() {
        final List<IColumn<AlertBean, String>> columns = new ArrayList<IColumn<AlertBean, String>>();

        columns.add(new PropertyColumn<AlertBean, String>(Model.of("Enabled"), "enabled", "enabled") {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<AlertBean>> item, final String componentId, final IModel<AlertBean> rowModel) {
                final AlertBean bean = rowModel.getObject();

                final Fragment linkFragment = new Fragment(componentId, "enabled-fragment", AlertPage.this);
                final AjaxCheckBox checkBox = new AjaxCheckBox("enabled", new PropertyModel<Boolean>(bean, "enabled")) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onUpdate(final AjaxRequestTarget target) {
                        LOG.debug("Changing state for {}", bean);

                        // the state is already flipped by the time it reaches here
                        if(bean.isEnabled()) {
                            info("Enabled: " + bean.getAlertQuery());
                        } else {
                            info("Disabled: " + bean.getAlertQuery());
                        }

                        target.add(getFeedbackPanel());
                        target.add(this);

                        // update the bean
                        try {
                            queryRunner.update(AlertBean.class, bean);
                        } catch (SQLException e) {
                            LOG.error("Error updating alert bean {}: {}", bean.getAlertId(), e.getMessage());
                            getSession().error("Error updating alert");
                        }
                    }

                };

                checkBox.setOutputMarkupId(true);
                linkFragment.add(checkBox);
                item.add(linkFragment);
            }
        });

        columns.add(new PropertyColumn<AlertBean, String>(Model.of("Query"), "alertQuery", "alertQuery") {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<AlertBean>> item, final String componentId, final IModel<AlertBean> rowModel) {
                final AlertBean bean = rowModel.getObject();

                final Fragment linkFragment = new Fragment(componentId, "link-fragment", AlertPage.this);
                final AjaxLink<AlertBean> link = new AjaxLink<AlertBean>("link", rowModel) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        alertBeanModel.setObject(bean);
                        target.add(form);

                        alertButton.setModel(Model.of("Update Alert"));
                        target.add(alertButton);

                        target.appendJavaScript("$(resizeSearchQuery);");
                    }

                };

                link.setOutputMarkupId(true);
                link.add(new Label("query", bean.getAlertQuery()));
                linkFragment.add(link);
                item.add(linkFragment);
            }
        });

        columns.add(new HeaderlessColumn<AlertBean, String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<AlertBean>> item, String componentId, IModel<AlertBean> rowModel) {
                final AlertBean bean = rowModel.getObject();

                final Fragment linkFragment = new Fragment(componentId, "delete-fragment", AlertPage.this);

                final Link<String> delete = new Link<String>("delete") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        try {
                            queryRunner.delete(AlertBean.class, bean);
                            getSession().success("Alert deleted!");
                        } catch (SQLException e) {
                            LOG.error("Error deleting alert {}: {}", bean.getAlertId(), e.getMessage());
                            getSession().error("Error deleting alert");
                        }
                        this.setResponsePage(AlertPage.class);
                    }
                };

                linkFragment.add(delete);
                item.add(linkFragment);
            }

        });

        final BootstrapAjaxDataTable<AlertBean, String> table
            = new BootstrapAjaxDataTable<AlertBean, String>("alert-table",
                                                            columns,
                                                            dataProviderFactory.create(AlertBean.class, DEFAULT_SORT),
                                                            20);

        add(table);
    }

    protected void setupForm() {
        form = new Form<AlertBean>("form") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                final AlertBean alertBean = alertBeanModel.getObject();

                LOG.debug("ALERT: {}", alertBean.toString());

                final MetrinkSession session = (MetrinkSession) getSession();

                AlertQuery alertQuery = null;

                try {
                    // parse the alert to ensure it's properly formed
                    alertQuery = alertQueryParser.createAlertQuery(alertBean.getAlertQuery());
                } catch (final MetrinkParseException e) {
                    session.error(e.getMessage());
                    return;
                }

                final String actionName = alertQuery.getActionName();
                ActionBean actionBean;

                try {
                    actionBean = queryRunner.query("select * from " + EntityUtils.getTableName(ActionBean.class) + " where actionName = :actionName")
                                    .bind("actionName", actionName)
                                    .execute(new BeanHandler<ActionBean>(ActionBean.class));
                } catch (SQLException e) {
                    throw new WicketRuntimeException("Error reading action bean: " + e.getMessage());
                }

                // make sure the action exists
                if(actionBean == null) {
                    session.error("Unknown action name: " + actionName);
                    return;
                }

                // force the bean to enabled
                alertBean.setEnabled(true);

                if(alertBean.getAlertId() != null) {
                    // update the alert
                    try {
                        queryRunner.update(AlertBean.class, alertBean);
                        info("Updated alert: " + alertBean.getAlertQuery());
                    } catch (SQLException e) {
                        LOG.error("Error updating alert {}: {}", alertBean.getAlertId(), e.getMessage());
                        error("Error updating alert");
                    }
                } else {
                    // create the entry in the db
                    try {
                        queryRunner.create(AlertBean.class, alertBean);
                        info("Created alert: " + alertBean.getAlertQuery());
                    } catch (SQLException e) {
                        LOG.error("Error creating alert {}: {}", alertBean.getAlertId(), e.getMessage());
                        error("Error creating alert");
                    }
                }

                alertBeanModel.setObject(new AlertBean());
                alertButton.setModel(Model.of("Add Alert"));
            }

        };

        final TextField<String> alertQuery =
                new TextField<String>("alert-query", PropertyModel.<String>of(alertBeanModel, "alertQuery"));
        alertQuery.setRequired(true);
        alertQuery.setOutputMarkupId(true);

        form.add(alertQuery);

        alertButton = new Button("alert-button", Model.of("Add Alert"));

        form.add(alertButton);
        form.add(new AjaxButton("cancel-button", form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> submitForm) {
                LOG.debug("Clearing form");

                // set the model to a new object
                alertBeanModel.setObject(new AlertBean());

                form.modelChanged();
                form.clearInput();

                target.add(form);

                // update the button
                alertButton.setModel(Model.of("Add Alert"));
                target.add(alertButton);

                target.appendJavaScript("$(resizeSearchQuery);");
            }

        }.setDefaultFormProcessing(false));

        add(form);
    }


    @Override
    public String getPageTitle() {
        return "Alerting";
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        LOG.debug("Deserializing AlertPage");
        in.defaultReadObject();
        final Injector injector = ((com.metrink.gui.Application)getApplication()).getInjector();
        queryRunner = injector.getInstance(QueryRunner.class);
        dataProviderFactory = injector.getInstance(DbDataProviderFactory.class);
        alertQueryParser = injector.getInstance(AlertQueryParser.class);
    }
}
