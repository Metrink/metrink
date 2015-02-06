package com.metrink.gui.component;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

/**
 * Copy and Paste from {@link AjaxFallbackDefaultDataTable} with bootstrap modifications.
 * @param <T>
 * @param <S>
 */
public class BootstrapAjaxDataTable<T, S> extends DataTable<T, S> {

    private static final long serialVersionUID = 1L;

    public BootstrapAjaxDataTable(final String id, final List<? extends IColumn<T, S>> columns,
            final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setVersioned(false);
        addTopToolbar(new BootstrapAjaxNavigationToolbar(this));
        addTopToolbar(new AjaxFallbackHeadersToolbar<S>(this, dataProvider));
        addBottomToolbar(new NoRecordsToolbar(this));
    }

    @Override
    protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
        return new OddEvenItem<T>(id, index, model);
    }

}