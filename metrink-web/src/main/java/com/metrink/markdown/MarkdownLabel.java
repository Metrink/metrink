package com.metrink.markdown;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.google.inject.Inject;

/**
 * Label which will render HTML from a {@link IModel}<String> with a markdown processor.
 */
public class MarkdownLabel extends Label {
    private static final long serialVersionUID = 1L;

    /**
     * Initialize the class.
     * @param id the wicket id
     * @param model the String model
     * @param markdown the markdown processor
     */
    public MarkdownLabel(final String id, final IModel<String> model, final Markdown markdown) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        setEscapeModelStrings(false);
    }

    @Override
    public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
        final String markdown = (String)getDefaultModelObject();
        final String html = new Markdown().markdownToHtml(markdown);
        replaceComponentTagBody(markupStream, openTag, html);
    }

    /**
     * Factory for creating {@link MarkdownLabel}s.
     */
    public static class MarkdownLabelFactory {
        private final Markdown markdown;

        /**
         * Initialize the factory
         * @param markdown the markdown processor
         */
        @Inject
        public MarkdownLabelFactory(final Markdown markdown) {
            this.markdown = markdown;
        }

        /**
         * Construct a new MarkdownLabel
         * @param id the wicket id
         * @param model the string model
         * @return new instance
         */
        public MarkdownLabel build(final String id, final IModel<String> model) {
            return new MarkdownLabel(id, model, markdown);
        }
    }
}