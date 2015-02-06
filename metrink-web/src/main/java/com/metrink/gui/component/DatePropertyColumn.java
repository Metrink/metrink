package com.metrink.gui.component;

import java.util.Date;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.metrink.gui.MetrinkSession;
import com.metrink.metric.User;

/**
 * A {@link PropertyColumn} that converts from UTC to the User's local time.
 *
 * @param <S> the sort property
 */
public class DatePropertyColumn<S> extends PropertyColumn<User, S> {
    private static final long serialVersionUID = 1L;
    //private static final Logger LOG = LoggerFactory.getLogger(DatePropertyColumn.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("M/d/y h:mm a");

    public DatePropertyColumn(IModel<String> displayModel, S sortProperty, String propertyExpression) {
        super(displayModel, sortProperty, propertyExpression);
    }

    public DatePropertyColumn(IModel<String> displayModel, String propertyExpression) {
        super(displayModel, propertyExpression);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IModel<Object> getDataModel(final IModel<User> rowModel) {
        final User user = rowModel.getObject();

        // we want to display in the timezone of the current user
        final DateTimeZone timeZone = DateTimeZone.forID(MetrinkSession.getCurrentUser().getTimezone());

        Date date = null;

        switch(getPropertyExpression()) {
        case "created":
            date = user.getCreated();
            break;

        case "lastLogin":
            date = user.getLastLogin();
            break;

        default:
            throw new IllegalArgumentException("Unknown property expression: " + getPropertyExpression());
        }

        if(date == null) {
            return null;
        }

        //
        // MySQL returns timestamps in the timezone of the connection (machine)
        // http://dev.mysql.com/doc/refman/5.5/en/datetime.html
        // DateTime & Date.getTime() both use UTC (actually GMT), so we're safe here
        // then .toDateTime(timeZone) converts to the user's timezone
        //
        final DateTime dateTime = new DateTime(date.getTime()).toDateTime(timeZone);

        return new Model(DATE_FORMAT.print(dateTime));
    }

}
