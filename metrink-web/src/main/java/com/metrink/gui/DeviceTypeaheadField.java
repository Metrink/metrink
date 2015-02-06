package com.metrink.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.IModel;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.metrink.gui.bootstrap.BootstrapTypeaheadField;
import com.metrink.metric.io.MetricMetadata;

/**
 * A BootstrapTypeaheadField that provides the devices associated with the owner.
 */
public class DeviceTypeaheadField extends BootstrapTypeaheadField {

    private static final long serialVersionUID = 1477796723359177155L;
    private final MetricMetadata metricMetadata;

    public DeviceTypeaheadField(final MetricMetadata metricMetadata,
                                final String id,
                                final IModel<String> model) {
        super(id, model);

        this.metricMetadata = metricMetadata;
    }

    @Override
    public Iterator<String> getChoices(final String query) {
        if(StringUtils.isBlank(query)) {
            return Collections.emptyIterator();
        }

        // this is probably a bit slow
        final List<String> devices = metricMetadata.readUniqueDevices();
        final Pattern pattern = Pattern.compile(".*" + query + ".*");

        // go through the collection and pull out the matches
        final Collection<String> matches = Collections2.filter(devices, new Predicate<String>() {
            @Override
            public boolean apply(final String input) {
                if(pattern.matcher(input).matches()) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        return matches.iterator();
    }

}
