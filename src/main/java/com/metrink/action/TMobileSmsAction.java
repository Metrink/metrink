package com.metrink.action;

import org.apache.commons.mail.SimpleEmail;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TMobileSmsAction extends SmsAction {
    @Inject
    public TMobileSmsAction(Provider<SimpleEmail> emailProvider) {
        super(emailProvider);
    }

    @Override
    protected String constructAddress(String phoneNumber) {
        return phoneNumber + "@tmomail.net";
    }
}
