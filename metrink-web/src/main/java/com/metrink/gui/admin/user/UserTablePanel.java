package com.metrink.gui.admin.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.gui.admin.user.UserDataProvider.UserDataProviderFactory;
import com.metrink.gui.component.BootstrapAjaxDataTable;
import com.metrink.gui.component.DatePropertyColumn;
import com.metrink.metric.User;

public class UserTablePanel extends Panel {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(UserTablePanel.class);

    private static final SortParam<String> DEFAULT_SORT = new SortParam<String>("name", true);

    private String searchQuery;

    public UserTablePanel(final String id,
                          final PageParameters params,
                          final UserDataProviderFactory dataProviderFactory,
                          final UserFormPanel userFormPanel) {
        super(id);

        this.searchQuery = params.get("search").toString();

        setupTable(userFormPanel, dataProviderFactory);
        setupForm();
    }

    protected void setupForm() {
        final StatelessForm<String> searchForm = new StatelessForm<String>("search-form") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                final PageParameters params = new PageParameters();

                params.add("search", searchQuery);

                this.setResponsePage(this.getPage().getClass(), params);
            }
        };

        searchForm.add(new TextField<String>("search", PropertyModel.<String>of(this, "searchQuery")));
        searchForm.add(new Button("clear-button") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                this.setResponsePage(this.getPage().getClass(), new PageParameters());
            }
        }.setDefaultFormProcessing(false));

        add(searchForm);
    }

    protected void setupTable(final UserFormPanel userFormPanel, final UserDataProviderFactory dataProviderFactory) {
        final List<IColumn<User, String>> columns = new ArrayList<IColumn<User, String>>();

        columns.add(new AbstractColumn<User, String>(Model.of("Name")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<User>> item,
                                     final String componentId,
                                     final IModel<User> rowModel) {

                final User user = rowModel.getObject();
                final Fragment linkFragment = new Fragment(componentId, "name-fragment", UserTablePanel.this);

                linkFragment.add(new AjaxLink<User>("name-link", rowModel) {
                   private static final long serialVersionUID = -1206619334473876487L;

                   @Override
                   public void onClick(final AjaxRequestTarget target) {
                       userFormPanel.setForm(user, target);
                   }
                }.add(new Label("name", user.getName())));

                item.add(linkFragment);
            }
        });

        columns.add(new DatePropertyColumn<String>(Model.of("Created"), "created", "created"));
        columns.add(new DatePropertyColumn<String>(Model.of("LastLogin"), "lastLogin", "lastLogin"));
        columns.add(new PropertyColumn<User, String>(Model.of("Roles"), "roles", "roles"));

        final BootstrapAjaxDataTable<User, String> table
            = new BootstrapAjaxDataTable<User, String>("user-table",
                                                       columns,
                                                       dataProviderFactory.create(DEFAULT_SORT, searchQuery),
                                                       20);

        add(table);
    }
}
