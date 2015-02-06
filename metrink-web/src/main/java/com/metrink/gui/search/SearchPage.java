package com.metrink.gui.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.metrink.gui.component.BootstrapAjaxDataTable;
import com.metrink.gui.search.MetricDataProvider.MetricDataProviderFactory;
import com.metrink.gui.stilearn.StiLearnPage;
import com.metrink.metric.MetricId;

public class SearchPage extends StiLearnPage {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SearchPage.class);

    private static final SortParam<String> DEFAULT_SORT = new SortParam<String>("device", true);

    private Form<String> form;
    private Button searchButton;
    private String searchQuery;

    @Inject
    public SearchPage(final PageParameters params,
                      final MetricDataProviderFactory dataProviderFactory) {

        this.searchQuery = params.get("search").toString();

        setupForm();
        setupTable(dataProviderFactory);
    }

    protected void setupTable(final MetricDataProviderFactory dataProviderFactory) {
        final List<IColumn<MetricId, String>> columns = new ArrayList<IColumn<MetricId, String>>();

        columns.add(new PropertyColumn<MetricId, String>(Model.of("Device"), "device", "device"));
        columns.add(new PropertyColumn<MetricId, String>(Model.of("Group"), "groupName", "groupName"));
        columns.add(new PropertyColumn<MetricId, String>(Model.of("Name"), "name", "name"));

        final BootstrapAjaxDataTable<MetricId, String> table
            = new BootstrapAjaxDataTable<MetricId, String>("metrics-table",
                                                           columns,
                                                           dataProviderFactory.create(DEFAULT_SORT, searchQuery),
                                                           20);

        add(table);
    }

    protected void setupForm() {
        form = new Form<String>("form") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                LOG.debug("Search: {}", searchQuery);

                final PageParameters params = new PageParameters();

                params.add("search", searchQuery);

                this.setResponsePage(SearchPage.class, params);
            }

        };

        final TextField<String> searchQuery =
                new TextField<String>("search", PropertyModel.<String>of(this, "searchQuery"));
        searchQuery.setRequired(true);
        searchQuery.setOutputMarkupId(true);

        form.add(searchQuery);

        searchButton = new Button("search-button", Model.of("Search"));

        form.add(searchButton);

        add(form);
    }


    @Override
    public String getPageTitle() {
        return "Searching";
    }

/*
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        LOG.debug("Deserializing AlertPage");
        in.defaultReadObject();
        final Injector injector = ((com.metrink.gui.Application)getApplication()).getInjector();
        dataProviderFactory = injector.getInstance(MetricDataProviderFactory.class);
    }
*/
}
