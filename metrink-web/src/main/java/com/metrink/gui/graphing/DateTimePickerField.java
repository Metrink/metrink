package com.metrink.gui.graphing;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimePickerField extends TextField<DateTime> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DateTimePickerField.class);

    public static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm aa");

    public DateTimePickerField(final String id, final IModel<DateTime> model) {
        super(id, model);
    }

    @Override
    protected void convertInput() {
        final String input = getRawInput();

        if(StringUtils.isEmpty(input)) {
            this.setConvertedInput(null);
            return;
        }


        try {
            final DateTime date = FORMAT.parseDateTime(input);
            this.setConvertedInput(date);
        } catch(final IllegalArgumentException e) {
            LOG.error("Error converting date {}: {}", input, e.getMessage());
            this.setConvertedInput(null);
            this.getForm().error("The value of " + this.getDefaultLabel() + " is not a valid date.");
        }
    }

    @Override
    protected String getModelValue() {
        final DateTime date = this.getModelObject();

        if(date == null) {
            return null;
        } else {
            return FORMAT.print(date);
        }
    }

}
