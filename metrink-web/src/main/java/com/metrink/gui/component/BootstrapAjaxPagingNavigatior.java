package com.metrink.gui.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.navigation.paging.IPageable;

public final class BootstrapAjaxPagingNavigatior extends AjaxPagingNavigator {
    private final DataTable<?, ?> table;
    private static final long serialVersionUID = 1L;

    public BootstrapAjaxPagingNavigatior(final String id, final IPageable pageable, final DataTable<?, ?> table) {
        super(id, pageable);
        this.table = table;
    }

    @Override
    protected void onAjaxEvent(final AjaxRequestTarget target) {
        target.add(table);
    }
}