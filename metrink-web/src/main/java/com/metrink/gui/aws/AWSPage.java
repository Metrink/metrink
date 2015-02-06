package com.metrink.gui.aws;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClient;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.metrink.gui.stilearn.StiLearnPage;
import com.google.inject.Inject;

public class AWSPage extends StiLearnPage {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AWSPage.class);
    
    @Inject private transient AmazonCloudWatchAsyncClient cloudWatch;
    
    private String accessKey;
    private String secretKey;

    @Inject
    public AWSPage() {
        //this.cloudWatch = cloudWatch;
        
        Injector.get().inject(this);
        
        final Form<Void> form = new Form<Void>("form");
        
        form.add(new RequiredTextField<String>("access-key", PropertyModel.<String>of(this, "accessKey")));
        form.add(new RequiredTextField<String>("secret-key", PropertyModel.<String>of(this, "secretKey")));
        form.add(new AjaxButton("fetch-button") {

            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                LOG.debug("ACCESS-KEY: {}", accessKey);
                final ListMetricsRequest request = new ListMetricsRequest();
                
                request.setRequestCredentials(new BasicAWSCredentials(accessKey, secretKey));
                
                final ListMetricsResult result = cloudWatch.listMetrics(request);
                
                for(Metric metric:result.getMetrics()) {
                    LOG.debug("METRIC: {}", metric);
                }
            }
            
        });
        
        this.add(form);
    }
    
    @Override
    protected String getPageTitle() {
        return "Administrator Settings";
    }
}
