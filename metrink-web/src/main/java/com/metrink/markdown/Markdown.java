package com.metrink.markdown;


import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * Perform processing to convert markdown to HTML.
 */
public class Markdown
{
    /**
     * Convert markdown to HTML. This is abstracted to allow customizations as needed.
     * @param markdown the markdown
     * @return the html
     */
    public String markdownToHtml(final String markdown)
    {
        final PegDownProcessor processor = new PegDownProcessor(Extensions.TABLES | Extensions.FENCED_CODE_BLOCKS);
        return processor.markdownToHtml(markdown)
                // HACK: the latest version of Pegdown supports plugins, but this conflicts with another plugin...
                .replaceAll("blockquote><p>info", "blockquote class=\"alert alert-info\"><p>")
                .replaceAll("blockquote><p>warning", "blockquote class=\"alert\"><p>")
                .replaceAll("blockquote><p>danger", "blockquote class=\"alert alert-danger\"><p>")
                .replaceAll("<table>", "<table class=\"table table-bordered table-condensed table-striped\"");
    }
}