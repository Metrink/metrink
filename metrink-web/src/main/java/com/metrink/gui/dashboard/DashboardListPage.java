package com.metrink.gui.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Inject;
import com.metrink.dashboard.DashboardBean;
import com.metrink.db.DbDataProvider.DbDataProviderFactory;
import com.metrink.gui.component.BootstrapAjaxDataTable;
import com.metrink.gui.stilearn.StiLearnPage;

public class DashboardListPage extends StiLearnPage {
    //private static final Logger LOG = LoggerFactory.getLogger(DashboardListPage.class);
    private static final long serialVersionUID = 1L;

    private static final SortParam<String> DEFAULT_SORT = new SortParam<String>("dashboardName", true);

    @Inject
    public DashboardListPage(final DbDataProviderFactory dataProviderFactory) {

        final List<IColumn<DashboardBean, String>> columns = new ArrayList<IColumn<DashboardBean, String>>();
        columns.add(new PropertyColumn<DashboardBean, String>(Model.of("Name"), "dashboardName", "dashboardName") {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<DashboardBean>> item,
                                     final String componentId,
                                     final IModel<DashboardBean> rowModel) {
                final DashboardBean bean = rowModel.getObject();

                final PageParameters linkPP = new PageParameters(getPageParameters())
                    .add("dashboardId", bean.getDashboardId())
                    .add("dashboardName", bean.getStubName());

                item.add(
                    new Fragment(componentId, "link-fragment", DashboardListPage.this).add(
                        new BookmarkablePageLink<Page>("link", DashboardPage.class, linkPP).add(
                            new Label("text", bean.getDashboardName()))));
            }
        });

        final DataTable<DashboardBean, String> table = new BootstrapAjaxDataTable<DashboardBean, String>(
                "table",
                columns,
                dataProviderFactory.create(DashboardBean.class, DEFAULT_SORT),
                20);

        add(table);
        add(new BookmarkablePageLink<Page>("create", DashboardModifyPage.class));
    }

    @Override
    public String getPageTitle() {
        return "Dashboards";
    }

}
