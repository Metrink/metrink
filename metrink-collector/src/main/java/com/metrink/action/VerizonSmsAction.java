package com.metrink.action;

import org.apache.commons.mail.SimpleEmail;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class VerizonSmsAction extends SmsAction {
    //private static final Logger LOG = LoggerFactory.getLogger(VerizonSmsAction.class);

    @Inject
    public VerizonSmsAction(Provider<SimpleEmail> emailProvider) {
        super(emailProvider);
    }

    @Override
    protected String constructAddress(String phoneNumber) {
        return phoneNumber + "@vtext.com";
    }
}
