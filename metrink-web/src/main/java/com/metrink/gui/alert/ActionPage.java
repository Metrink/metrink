package com.metrink.gui.alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.metrink.alert.ActionBean;
import com.metrink.db.DbDataProvider;
import com.metrink.db.DbDataProvider.DbDataProviderFactory;
import com.metrink.gui.component.BootstrapAjaxDataTable;
import com.metrink.gui.stilearn.StiLearnPage;
import com.metrink.utils.PageParameterUtils;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.sop4j.dbutils.QueryRunner;


public class ActionPage extends StiLearnPage {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ActionPage.class);

    private static final SortParam<String> DEFAULT_SORT = new SortParam<String>("actionId", true);

    private transient QueryRunner queryRunner;
    private transient DbDataProviderFactory dataProviderFactory;

    private final IModel<ActionBean> actionModel;
    private Form<ActionBean> form;
    private TextField<String> actionName;
    private Button updateSaveButton;

    @Inject
    public ActionPage(final PageParameters params,
                      final QueryRunner queryRunner,
                      final DbDataProviderFactory dataProviderFactory) {

        this.queryRunner = queryRunner;
        this.dataProviderFactory = dataProviderFactory;

        final ActionBean actionBean = PageParameterUtils.paramsToBean(ActionBean.class, params);

        this.actionModel = Model.of(actionBean);

        setupForm();
        setupTable();
    }

    protected void setupTable() {
        final List<IColumn<ActionBean, String>> columns = new ArrayList<IColumn<ActionBean, String>>();

        columns.add(new PropertyColumn<ActionBean, String>(Model.of("Name"), "actionName", "actionName") {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<ActionBean>> item, final String componentId, final IModel<ActionBean> rowModel) {
                final ActionBean bean = rowModel.getObject();

                final Fragment linkFragment = new Fragment(componentId, "link-fragment", ActionPage.this);
                final Label label = new Label("text", bean.getActionName());
                final AjaxLink<String> link = new AjaxLink<String>("link", Model.of(bean.getActionName())) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        LOG.debug("Click for {}", bean);

                        // fill in the form
                        ActionPage.this.setForm(bean, target);
                    }

                };

                label.setOutputMarkupId(true);
                link.add(label);
                linkFragment.add(link);
                item.add(linkFragment);
            }
        });

        columns.add(new PropertyColumn<ActionBean, String>(Model.of("Type"), "type", "type"));
        columns.add(new PropertyColumn<ActionBean, String>(Model.of("Value"), "value", "value"));

        columns.add(new HeaderlessColumn<ActionBean, String>() {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<ActionBean>> item, final String componentId, final IModel<ActionBean> rowModel) {
                final ActionBean bean = rowModel.getObject();

                final Fragment linkFragment = new Fragment(componentId, "delete-fragment", ActionPage.this);

                final Link<String> link = new Link<String>("delete") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        LOG.debug("Deleting action: {}", bean);

                        try {
                            queryRunner.delete(ActionBean.class, bean);
                            getSession().success("Action " + bean.getActionName() + " deleted!");
                        } catch (SQLException e) {
                            // we don't throw here because it's not the end of the world
                            LOG.error("Error deleteing from DB");
                            getSession().error("Error deleting action " + bean.getActionName() + " deleted!");
                        }

                        this.setResponsePage(ActionPage.class);
                    }

                };

                linkFragment.add(link);
                item.add(linkFragment);
            }
        });

        final DbDataProvider<ActionBean> dataProvider =
            dataProviderFactory.create(ActionBean.class, DEFAULT_SORT);

        final BootstrapAjaxDataTable<ActionBean, String> table =
            new BootstrapAjaxDataTable<ActionBean, String>("actions-table",
                                                                  columns,
                                                                  dataProvider,
                                                                  20);

        add(table);
    }

    protected void setupForm() {
        form = new Form<ActionBean>("form", actionModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                final ActionBean bean = actionModel.getObject();

                LOG.debug("Action: {}", bean.toString());

                //
                // Sanity check our bean
                //

                if(StringUtils.containsWhitespace(bean.getActionName()) ||
                   !StringUtils.isAlphanumericSpace(bean.getActionName())) {
                    getSession().error("Action names cannot have spaces or special characters");

                    final PageParameters params = PageParameterUtils.beanToParams(bean);

                    setResponsePage(ActionPage.class, params);
                    return;
                }

                if(bean.getType().endsWith("SMS")) {
                    try {
                        PhoneNumberUtil.getInstance().parse(bean.getValue(), "US");
                    } catch (final NumberParseException e) {
                        getSession().error("Invalid US phone number: " + e.getMessage());

                        final PageParameters params = PageParameterUtils.beanToParams(bean);

                        this.setResponsePage(ActionPage.class, params);
                        return;
                    }
                } else {
                    if(! EmailValidator.getInstance().isValid(bean.getValue())) {
                        getSession().error("Invalid email address");

                        final PageParameters params = PageParameterUtils.beanToParams(bean);

                        this.setResponsePage(ActionPage.class, params);
                        return;
                    }
                }

                // decide if we're creating a new one or updating
                if(bean.getActionId() == null) {
                    try {
                        queryRunner.create(ActionBean.class, bean);
                        LOG.info("Action {} created", bean);
                        getSession().success(bean.getActionName() + " was added");
                    } catch(final SQLException e) {
                        final String msg = e.getLocalizedMessage();

                        LOG.error("Error creating action: {}", msg);

                        final Throwable t = e.getCause();

                        // crappy way to clean-up this message, but it works
                        if(t instanceof MySQLIntegrityConstraintViolationException) {
                            getSession().error("Cannot create two actions with the same name: " + bean.getActionName());
                        } else {
                            getSession().error("Error creating action: " + e.getMessage());
                        }
                    }
                } else { // updating
                    try {
                        queryRunner.update(ActionBean.class, bean);
                        LOG.info("Action {} updated", bean);
                        getSession().success(bean.getActionName() + " was updated");
                    } catch(final SQLException e) {
                        LOG.error("Error updating action: {}", e.getMessage());
                        getSession().error("Error updating action: " + e.getMessage());
                    }
                }

                this.setResponsePage(ActionPage.class);
            }

        };

        actionName = new TextField<>("action-name", PropertyModel.<String>of(actionModel, "actionName"));
        actionName.setOutputMarkupId(true);
        actionName.setRequired(true);

        final DropDownChoice<String> actionType =
                new DropDownChoice<>("action-type", PropertyModel.<String>of(actionModel, "type"), Arrays.asList(ActionBean.ACTION_TYPES));
        actionType.setRequired(true);
        actionType.setOutputMarkupId(true);

        final TextField<String> actionValue = new TextField<>("action-value", PropertyModel.<String>of(actionModel, "value"));
        actionValue.setRequired(true);
        actionValue.setOutputMarkupId(true);

        updateSaveButton = new Button("update-button", Model.of("Add Action"));

        // add all the components to the form
        form.add(actionName);
        form.add(actionType);
        form.add(actionValue);
        form.add(updateSaveButton);
        form.add(new AjaxButton("cancel-button", form) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> submitForm) {
                LOG.debug("Resetting form");

                // set the form to a blank bean
                ActionPage.this.clearForm(target);
            }
        }.setDefaultFormProcessing(false));

        add(form);
    }

    public void setForm(final ActionBean bean, final AjaxRequestTarget target) {
        LOG.debug("Setting form to {}", bean);

        // update the model with the new bean
        actionModel.setObject(bean);

        // disable the name
        actionName.setEnabled(false);
        target.add(actionName);

        // add the form to the target
        target.add(form);

        // update the button
        updateSaveButton.setModel(Model.of("Update"));
        target.add(updateSaveButton);
    }

    public void clearForm(final AjaxRequestTarget target) {
        LOG.debug("Clearing form");

        // set the model to a new object
        actionModel.setObject(new ActionBean());

        // re-enable the action name
        actionName.setEnabled(true);
        target.add(actionName);

        form.modelChanged();
        form.clearInput();

        target.add(form);

        // update the button
        updateSaveButton.setModel(Model.of("New"));
        target.add(updateSaveButton);
    }


    @Override
    public String getPageTitle() {
        return "Actions";
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final Injector injector = ((com.metrink.gui.Application)getApplication()).getInjector();
        queryRunner = injector.getInstance(QueryRunner.class);
        dataProviderFactory = injector.getInstance(DbDataProviderFactory.class);
    }
}
