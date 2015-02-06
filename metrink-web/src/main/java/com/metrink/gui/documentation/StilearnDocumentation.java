package com.metrink.gui.documentation;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metrink.gui.stilearn.StiLearnPage;
import com.metrink.markdown.MarkdownLabel;
import com.metrink.markdown.MarkdownLabel.MarkdownLabelFactory;
import com.metrink.markdown.StringResourceModel;
import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Documentation styled with the {@link StilearnBorder}. Renders the markdown file located in md/${page}.md.
 */
public class StilearnDocumentation extends StiLearnPage {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(StilearnDocumentation.class);

    /**
     * Initialize the markdown page.
     * @param pageParameters containing the page argument which is the markdown filename without an extension
     * @param markdownLabelFactory factory used to build {@link MarkdownLabel}s.
     */
    @Inject
    public StilearnDocumentation(final PageParameters pageParameters, final MarkdownLabelFactory markdownLabelFactory) {
        final String pageParameter = pageParameters.get("page").toOptionalString();
        final String page = Strings.isNullOrEmpty(pageParameter) ? "index" : pageParameter;
        final StringResourceModel model = new StringResourceModel(getRoot(), "md/" + page + ".md");

        loadModelOr404(model);

        add(markdownLabelFactory.build("content", model));
    }

    /**
     * Return the point of reference to search from when looking for the md directory.
     * @return the class to use as the root directory
     */
    public Class<?> getRoot() {
        return StilearnDocumentation.class;
    }

    @Override
    protected String getPageTitle() {
        return "Documentation";
    }

    /**
     * Confirm that we can load the model, otherwise throw a 404 Not Found.
     * @param model the markdown model
     */
    private void loadModelOr404(final StringResourceModel model) {
        try {
            model.load();

        } catch (final WicketRuntimeException | IllegalArgumentException e) {
            LOG.warn("Failed to load markdown resource", e);
            throw new AbortWithHttpErrorCodeException(404, "Not Found");
        }
    }
}
