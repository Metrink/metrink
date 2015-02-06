package com.metrink.gui.bootstrap;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONWriter;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

public abstract class BootstrapTypeaheadField extends TextField<String> implements IResourceListener {

    private static final long serialVersionUID = 1L;

    public BootstrapTypeaheadField(final String id, final IModel<String> model) {
        super(id, model);

        setOutputMarkupId(true);
        add(new AttributeModifier("data-provide", "typeahead"));
        add(new AttributeModifier("autocomplete", "off"));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssReferenceHeaderItem.forReference(new CssResourceReference(BootstrapTypeaheadField.class, "css/BootstrapTypeaheadField.css")));

        final TextTemplate template = new PackageTextTemplate(BootstrapTypeaheadField.class, "js/BootstrapTypeaheadField.js");

        // configure all the variables for our template
        final Map<String, CharSequence> vars = new HashMap<String, CharSequence>();

        vars.put("param_name", this.getId());
        vars.put("listener_url", urlFor(IResourceListener.INTERFACE, null));
        vars.put("items", getMaxItems() + "");
        vars.put("min_length", getMinLength() + "");
        vars.put("component_id", getMarkupId().replace(".", "\\\\."));

        response.render(OnDomReadyHeaderItem.forScript(template.asString(vars)));

        try {
            template.close();
        } catch(final IOException e) {
            throw new WicketRuntimeException(e);
        }
    }

    public abstract Iterator<String> getChoices(final String query);

    public int getMaxItems() {
        return 100;
    }

    public int getMinLength() {
        return 1;
    }

    @Override
    public void onResourceRequested() {
        final Request request = getRequestCycle().getRequest();
        final IRequestParameters params = request.getRequestParameters();

        final String query = params.getParameterValue(this.getId()).toOptionalString();
        final Iterator<String> choiceIterator = getChoices(query);

        final WebResponse webResponse = (WebResponse) getRequestCycle().getResponse();
        webResponse.setContentType("application/json");

        final OutputStreamWriter out = new OutputStreamWriter(webResponse.getOutputStream(), getRequest().getCharset());
        final JSONWriter json = new JSONWriter(out);

        try {
            json.array();

            for(int i=0; choiceIterator != null && choiceIterator.hasNext(); ++i) {
                if(getMaxItems() != -1 && i > getMaxItems()) {
                    json.value("...");
                    break;
                }

                json.value(choiceIterator.next());
            }

            json.endArray();
        } catch (final JSONException e) {
            throw new WicketRuntimeException("Could not write Json response", e);
        }

        try {
            out.flush();
        } catch (final IOException e) {
            throw new WicketRuntimeException("Could not write Json to servlet response", e);
        }
    }
}
