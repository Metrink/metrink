package com.metrink.gui.stilearn;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.resource.bundles.ConcatResourceBundleReference;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.metrink.gui.BasePage;
import com.metrink.gui.MetrinkSession;
import com.metrink.gui.login.LogoutPage;
import com.metrink.gui.login.ResetPasswordPage;
import com.metrink.gui.signup.EditProfilePage;
import com.metrink.metric.User;

public abstract class StiLearnRootPage extends BasePage {
    private static final Logger LOG = LoggerFactory.getLogger(StiLearnRootPage.class);

    private static final String OPEN_SANS_FONT_CSS = "//fonts.googleapis.com/css?family=Open+Sans+Condensed:700";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm a");

    private static final long serialVersionUID = 1L;

    private static final List<String> STYLE_SHEETS = Arrays.asList(
            "css/bootstrap.css",
            "css/bootstrap-responsive.css",
            "css/stilearn.css",
            "css/stilearn-responsive.css",
            "css/stilearn-helper.css",
            "css/stilearn-icon.css",
            "css/font-awesome.css",
            "css/animate.css",
            "css/uniform.default.css",
            "override.css"
            );

    private static final List<UrlResourceReference> JAVASCRIPT_URLS = Arrays.asList(
            new UrlResourceReference(Url.parse("//platform.twitter.com/widgets.js")));

    private static final List<JavaScriptResourceReference> JAVASCRIPT_FILES = Arrays.asList(
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/jquery.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/jquery-migrate-1.1.1.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/jquery-ui.min.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/bootstrap.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/uniform/jquery.uniform.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/peity/jquery.peity.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/select2/select2.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/knob/jquery.knob.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/flot/jquery.flot.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/flot/jquery.flot.resize.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/flot/jquery.flot.categories.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/wysihtml5/wysihtml5-0.3.0.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/wysihtml5/bootstrap-wysihtml5.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/calendar/fullcalendar.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/holder.js"),
            new JavaScriptResourceReference(StiLearnRootPage.class, "js/stilearn-base.js"));

    public StiLearnRootPage() {
        final MetrinkSession metrinkSession = (MetrinkSession)getSession();
        /*
         * If they user hasn't logged in yet (login page for example), there won't be a user
         * but we still need values for these things, so we create a blank one.
         */
        User user = new User();

        if(metrinkSession.isSignedIn()) {
            user = metrinkSession.getUser();
        }

        add(new Label("username", user.getUsername()) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                // only show if logged in
                return metrinkSession.isSignedIn();
            }
        });

        add(new Label("name", user.getName()));

        final String createdTime = user.getCreated() == null ? "" : DATE_FORMAT.print(user.getCreated().getTime());
        add(new Label("created", createdTime));

        add(new BookmarkablePageLink<String>("password-link", ResetPasswordPage.class));

        add(new BookmarkablePageLink<String>("logout-link", LogoutPage.class));
        add(new BookmarkablePageLink<String>("profile-link", EditProfilePage.class));
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        response.render(CssReferenceHeaderItem.forReference(new UrlResourceReference(Url.parse(OPEN_SANS_FONT_CSS))));

        for (final String styleSheet : STYLE_SHEETS) {
            response.render(CssReferenceHeaderItem.forReference(new CssResourceReference(StiLearnRootPage.class, styleSheet)));
        }

        for(final ResourceReference js:JAVASCRIPT_URLS) {
            response.render(JavaScriptReferenceHeaderItem.forReference(js));
        }

        for(final ResourceReference js:JAVASCRIPT_FILES) {
            response.render(JavaScriptReferenceHeaderItem.forReference(js));
        }

        // TODO: I really dislike doing this, but chosen depends on jQuery. That means the child class needs to be
        // loaded first for this to work correctly. We should probably rethink this.
        super.renderHead(response);
    }

    public static ConcatResourceBundleReference<JavaScriptReferenceHeaderItem> getJavaScriptBundle() {
        final List<JavaScriptReferenceHeaderItem> items = Lists.newArrayList();

        for (final JavaScriptResourceReference file : JAVASCRIPT_FILES) {
            items.add(JavaScriptReferenceHeaderItem.forReference(file));
        }

        final ConcatResourceBundleReference<JavaScriptReferenceHeaderItem> bundleReference =
                new ConcatResourceBundleReference<JavaScriptReferenceHeaderItem>(StiLearnRootPage.class, "stilearn", items);

        return bundleReference;
    }

    public static ConcatResourceBundleReference<CssReferenceHeaderItem> getCssBundle() {
        final List<CssReferenceHeaderItem> items = Lists.newArrayList();

        for (final String file : STYLE_SHEETS) {
            items.add(CssReferenceHeaderItem.forReference(new CssResourceReference(StiLearnRootPage.class, file)));
        }

        final ConcatResourceBundleReference<CssReferenceHeaderItem> bundleReference =
                new ConcatResourceBundleReference<CssReferenceHeaderItem>(StiLearnRootPage.class, "css/stilearn", items);

        return bundleReference;
    }
}
