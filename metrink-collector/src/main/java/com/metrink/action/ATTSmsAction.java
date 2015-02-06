package com.metrink.action;

import org.apache.commons.mail.SimpleEmail;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ATTSmsAction extends SmsAction {
    //private static final Logger LOG = LoggerFactory.getLogger(ATTSmsAction.class);

    @Inject
    public ATTSmsAction(Provider<SimpleEmail> emailProvider) {
        super(emailProvider);
    }

    @Override
    protected String constructAddress(String phoneNumber) {
        return phoneNumber + "@txt.att.net";
    }
}
