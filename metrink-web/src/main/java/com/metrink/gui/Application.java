package com.metrink.gui;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.IInitializer;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.util.io.SerializableChecker;
import org.apache.wicket.core.util.objects.checker.CheckingObjectOutputStream;
import org.apache.wicket.core.util.objects.checker.IObjectChecker;
import org.apache.wicket.core.util.objects.checker.NotDetachedModelChecker;
import org.apache.wicket.devutils.stateless.StatelessChecker;
import org.apache.wicket.extensions.Initializer;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.resource.bundles.ConcatResourceBundleReference;
import org.apache.wicket.resource.loader.InitializerStringResourceLoader;
import org.apache.wicket.serialize.java.JavaSerializer;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.metrink.croquet.WicketSettings;
import com.metrink.croquet.wicket.CroquetApplication;
import com.metrink.gui.MetrinkSession.MetrinkSessionFactory;
import com.metrink.gui.admin.AdminPage;
import com.metrink.gui.alert.ActionPage;
import com.metrink.gui.alert.AlertPage;
import com.metrink.gui.dashboard.DashboardListPage;
import com.metrink.gui.dashboard.DashboardModifyPage;
import com.metrink.gui.dashboard.DashboardPage;
import com.metrink.gui.documentation.GettingStartedPage;
import com.metrink.gui.documentation.StilearnDocumentation;
import com.metrink.gui.graphing.GraphPage;
import com.metrink.gui.login.LoginPage;
import com.metrink.gui.login.LogoutPage;
import com.metrink.gui.login.ResetPasswordPage;
import com.metrink.gui.search.SearchPage;
import com.metrink.gui.signup.SignupPage;
import com.metrink.gui.stilearn.StiLearnRootPage;

public class Application extends CroquetApplication {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final MetrinkSessionFactory sessionFactory;
    private Injector injector;

    @Inject
    public Application(final IPageFactory pageFactory,
                       final WicketSettings wicketSettings,
                       final MetrinkSessionFactory sessionFactory,
                       final Injector injector) {
        super(pageFactory, wicketSettings);
        this.sessionFactory = sessionFactory;
        this.injector = injector;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return LoginPage.class;
    }

    @Override
    public void init() {
        super.init();

        // check the mode we're running in and configure a few things
        if(getConfigurationType().equals(RuntimeConfigurationType.DEVELOPMENT)) {
            LOG.debug("Statless checker added to application...");
            // add stateless checks if we're in dev mode
            this.getComponentPostOnBeforeRenderListeners().add(new StatelessChecker());

            LOG.debug("Adding serialization checker to application...");

            // Serialization checks
            // see https://cwiki.apache.org/confluence/display/WICKET/Serialization+Checker
            final JavaSerializer javaSerializer = new JavaSerializer(getApplicationKey()) {
                @Override
                protected ObjectOutputStream newObjectOutputStream(final OutputStream out) throws IOException
                {
                    final IObjectChecker checker = new NotDetachedModelChecker();
                    final IObjectChecker checker2 = new SerializableChecker.ObjectSerializationChecker();
                    return new CheckingObjectOutputStream(out, checker, checker2);
                }
            };
            getFrameworkSettings().setSerializer( javaSerializer );
        }

        // this removes the page versions from the URLs
        // see: http://ci.apache.org/projects/wicket/apidocs/6.0.x/org/apache/wicket/settings/IRequestCycleSettings.html
        this.getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);

        //
        // Total hack to get wicket extensions properties to load
        final List<IInitializer> initList = new ArrayList<IInitializer>();

        initList.add(new Initializer());
        this.getResourceSettings().getStringResourceLoaders().add(new InitializerStringResourceLoader(initList));


        this.mountPage("/login", LoginPage.class);
        this.mountPage("/reset-password/${email}/${time}/${hash}", ResetPasswordPage.class);
        this.mountPage("/logout", LogoutPage.class);
        this.mountPage("/signup", SignupPage.class);

        // main pages
        this.mountPage("/graphing/#{query}", GraphPage.class);
        this.mountPage("/dashboards", DashboardListPage.class);
        this.mountPage("/dashboards/${dashboardId}/#{dashboardName}", DashboardPage.class);
        this.mountPage("/dashboards/modify/#{dashboardId}", DashboardModifyPage.class);
        this.mountPage("/alerts", AlertPage.class);
        this.mountPage("/actions", ActionPage.class);
        this.mountPage("/search/#{query}", SearchPage.class);
        this.mountPage("/admin", AdminPage.class);

        // documentation
        this.mountPage("/integrated/documentation/${page}", StilearnDocumentation.class);
        this.mountPage("/integrated/documentation/getting-started", GettingStartedPage.class);

        // only install the exception handler stuff if we're in deployment mode
        if(getConfigurationType().equals(RuntimeConfigurationType.DEPLOYMENT)) {
            this.mountPage("/404", NotFoundPage.class);
            this.mountPage("/500", ExceptionPage.class);
            this.getRequestCycleListeners().add(new AbstractRequestCycleListener() {
                @Override
                public IRequestHandler onException(final RequestCycle cycle, final Exception e) {
                    LOG.error("Returning 500: {} {}", e.getMessage(), e);
                    return new RenderPageRequestHandler(new PageProvider(ExceptionPage.class));
                }
            });
        }

        //getMarkupSettings().setStripWicketTags(true);

        setHeaderResponseDecorator(new IHeaderResponseDecorator() {

            @Override
            public IHeaderResponse decorate(final IHeaderResponse response) {
                return new JavaScriptFilteredIntoFooterHeaderResponse(response, "javascript-footer");
            }

        });

        // Wicket bootstrap is serving a JQuery map file, which is used to debug
        // minified JavaScript. This results in a 500 when trying to access the
        // resource as that file type is not permitted. Until we can figure out
        // how to tell Wicket to not load that resource, this hack is needed.
        final IPackageResourceGuard packageResourceGuard = getResourceSettings().getPackageResourceGuard();
        if (packageResourceGuard instanceof SecurePackageResourceGuard) {
            final SecurePackageResourceGuard guard = (SecurePackageResourceGuard) packageResourceGuard;
            guard.addPattern("+*.map");
        }

        // Bundling stilearn resources into a single js & css file
        final ConcatResourceBundleReference<JavaScriptReferenceHeaderItem> stiLearnJavaScriptBundle
                = StiLearnRootPage.getJavaScriptBundle();
        final ConcatResourceBundleReference<CssReferenceHeaderItem> stiLearnCssBundle
                = StiLearnRootPage.getCssBundle();

        getResourceBundles().addBundle(CssHeaderItem.forReference(stiLearnCssBundle));
        getResourceBundles().addBundle(JavaScriptHeaderItem.forReference(stiLearnJavaScriptBundle));

        // Add session information to the log messages
        getRequestCycleListeners().add(new MetrinkMdcLogListener());

        LOG.info("Called Application.init()");
    }

    /**
     * Obtain the Applications injector for deserialization.
     * @return the injector
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * Create a new {@link MetrinkSession}.
     *
     * Not overriding {@link #getWebSessionClass()}, because it's not Guice enabled.
     */
    @Override
    public Session newSession(final Request request, final Response response) {
        return sessionFactory.create(request);
    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return MetrinkSession.class;
    }
}
