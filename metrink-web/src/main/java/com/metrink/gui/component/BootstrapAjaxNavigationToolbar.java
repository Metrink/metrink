package com.metrink.gui.component;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

public final class BootstrapAjaxNavigationToolbar extends AjaxNavigationToolbar {
    private static final long serialVersionUID = 1L;

    public BootstrapAjaxNavigationToolbar(final DataTable<?, ?> table) {
        super(table);
    }

    @Override
    protected PagingNavigator newPagingNavigator(final String navigatorId, final DataTable<?, ?> table) {
        return new BootstrapAjaxPagingNavigatior(navigatorId, table, table);
    }
}