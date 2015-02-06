package com.metrink.gui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.filter.HeaderResponseContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page adds the required JS & CSS for Bootstrap, Chosen, and OWA.
 */
public abstract class BasePage extends WebPage {

    private static final long serialVersionUID = 1L;
    private static Logger LOG = LoggerFactory.getLogger(BasePage.class);

    // our JavaScript resources
    private static final JavaScriptResourceReference CHOSEN_JS =
            new JavaScriptResourceReference(BasePage.class, "js/chosen-0.11.1.jquery.js");
    private static final JavaScriptResourceReference GOOGLE_ANALYTICS_JS =
            new JavaScriptResourceReference(BasePage.class, "js/google_analytics.js");
    private static final JavaScriptResourceReference CHOSEN_MIN_JS =
            new JavaScriptResourceReference(BasePage.class, "js/chosen-0.11.1.jquery.min.js");
    private static final JavaScriptResourceReference QTIP_MIN_JS =
            new JavaScriptResourceReference(BasePage.class, "js/jquery.qtip.min.js");


    // our CSS resources
    private static final CssResourceReference CHOSEN_CSS =
            new CssResourceReference(BasePage.class, "css/chosen-0.11.1.css");
    private static final CssResourceReference CHOSEN_MIN_CSS =
            new CssResourceReference(BasePage.class, "css/chosen-0.11.1.min.css");
    private static final CssResourceReference CHOSEN_OVERRIDE_CSS =
            new CssResourceReference(BasePage.class, "css/chosen-override.css");
    private static final CssResourceReference QTIP_MIN_CSS =
            new CssResourceReference(BasePage.class, "css/jquery.qtip.min.css");


    public BasePage() {
        this.setStatelessHint(true);

        // put all of the Javascript on the bottom
        add(new HeaderResponseContainer("javascript-footer", "javascript-footer"));

        // set the page's title
        add(new Label("pageTitle", Model.of("Metrink - " + getPageTitle())));
    }

    /**
     * Allows the page to set it's own title.
     * @return the title for the page.
     */
    protected abstract String getPageTitle();

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        // only include Google if we're in deployment mode
        if(this.getApplication().getConfigurationType().equals(RuntimeConfigurationType.DEPLOYMENT)) {
            response.render(JavaScriptReferenceHeaderItem.forReference(GOOGLE_ANALYTICS_JS));
        }

        // include chosen
        if(this.getApplication().getConfigurationType().equals(RuntimeConfigurationType.DEVELOPMENT)) {
            response.render(JavaScriptReferenceHeaderItem.forReference(CHOSEN_JS));
            response.render(CssReferenceHeaderItem.forReference(CHOSEN_CSS));
        } else {
            response.render(JavaScriptReferenceHeaderItem.forReference(CHOSEN_MIN_JS));
            response.render(CssReferenceHeaderItem.forReference(CHOSEN_MIN_CSS));
        }

        response.render(JavaScriptReferenceHeaderItem.forReference(QTIP_MIN_JS));
        response.render(CssReferenceHeaderItem.forReference(QTIP_MIN_CSS));

        response.render(CssReferenceHeaderItem.forReference(CHOSEN_OVERRIDE_CSS));
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        LOG.trace("Serializing {}", this.getClass());
        out.defaultWriteObject();
    }

    /**
     * Verify the restoration of transient fields on the {@link WebPage} base class. This is a sanity check to hopefully
     * avoid mistakes when pages get serialized. The check is skipped if we're not in development mode or if the class
     * implements its own readObject method. See metrink {@link https://bitbucket.org/wspeirs/metrink/issue/108} for
     *  impetus.
     * @param in object input stream
     * @throws IOException thrown by {@link ObjectInputStream#defaultReadObject()}
     * @throws ClassNotFoundException thrown by {@link ObjectInputStream#defaultReadObject()}
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        LOG.trace("Deserializing {}", this.getClass());
        in.defaultReadObject();

        if (getApplication().getConfigurationType().equals(RuntimeConfigurationType.DEVELOPMENT)) {
            for (final Method m : this.getClass().getDeclaredMethods()) {
                if (m.getName().equals("readObject")) {
                    // Java Serialization calls readObject looping from super to base class. If the sub-class properly
                    // implements readObject, this method will false positive, because the transient field hasn't had
                    // a chance to be restored.
                    LOG.trace("Child class implement readObject - short-circuiting transient check");
                    return;
                }
            }

            for (final Field f : this.getClass().getDeclaredFields()) {
                if (Modifier.isTransient(f.getModifiers())) {
                    LOG.trace("Found transient field: {}", f);
                    try {
                        f.setAccessible(true); // HACK to make private fields accessible - doesn't affect global scope
                        if (f.get(this) == null) {
                            LOG.warn("Transient field is null: {}", f);
                        }
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        LOG.error("Exception while trying to determine transient fields state: {}", e.getMessage());
                    }
                }
            }
        }
    }
}
