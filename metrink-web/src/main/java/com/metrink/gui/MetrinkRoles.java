package com.metrink.gui;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.Roles;

public class MetrinkRoles extends Roles {
    private static final long serialVersionUID = 1L;

    // USER is included from Roles
    public static final String ADMIN = "ADMIN"; // just use the existing

    public MetrinkRoles(final String role) {
        if(role == null) {
            return;
        }

        // add in that role and all the others "below" it
        switch(role) {
        case ADMIN:
            add(ADMIN);
        case USER:
            add(USER);
            break;
        }
    }

    public static List<String> getRoles() {
        return Arrays.asList(ADMIN, USER);
    }
}
