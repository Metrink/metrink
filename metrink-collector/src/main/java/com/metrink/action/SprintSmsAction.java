package com.metrink.action;

import org.apache.commons.mail.SimpleEmail;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class SprintSmsAction extends SmsAction {
    //private static final Logger LOG = LoggerFactory.getLogger(SprintSmsAction.class);

    @Inject
    public SprintSmsAction(Provider<SimpleEmail> emailProvider) {
        super(emailProvider);
    }

    @Override
    protected String constructAddress(String phoneNumber) {
        return phoneNumber + "@messaging.sprintpcs.com";
    }
}
