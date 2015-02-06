package com.metrink.gui.bootstrap;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;

public class BootstrapAjaxTabbedPanel<T extends ITab> extends AjaxTabbedPanel<T> {

    private static final long serialVersionUID = 1L;

    public BootstrapAjaxTabbedPanel(final String id, final List<T> tabs) {
        super(id, tabs);
    }

    @Override
    protected String getSelectedTabCssClass() {
        return "active";
    }

    @Override
    protected String getLastTabCssClass() {
        return "";
    }
}
