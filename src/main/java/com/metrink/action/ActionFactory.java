package com.metrink.action;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.metrink.alert.ActionBean;

public class ActionFactory {
    //private static final Logger LOG = LoggerFactory.getLogger(ActionFactory.class);

    private final Injector injector;

    @Inject
    public ActionFactory(Injector injector) {
        this.injector = injector;
    }

    /**
     * Given an action bean, create the appropriate type of action.
     * @param actionBean the action bean.
     * @return an {@link Action}.
     */
    public Action createAction(ActionBean actionBean) {
        switch(actionBean.getType()) {
        case "Email":
            return injector.getInstance(EmailAction.class);

        case "AT&T SMS":
            return injector.getInstance(ATTSmsAction.class);

        case "Sprint SMS":
            return injector.getInstance(SprintSmsAction.class);

        case "T-Mobile SMS":
            return injector.getInstance(TMobileSmsAction.class);

        case "Verizon SMS":
            return injector.getInstance(VerizonSmsAction.class);

        default:
            throw new IllegalArgumentException("Unknown action type: " + actionBean.getType());
        }
    }
}
