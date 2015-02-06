## Overview

With Metrink collecting and analyzing any metric is a breeze. Metrics are sent to Metrink via a RESTful API and stored on our servers. With Metrink you can slice and dice your metrics to provide the insight you're looking for. Using the powerful query language -- that includes metric transformations and time overlays -- you can quickly answer questions about your metrics. The same query language can be used to setup alerts allowing you to be emailed or paged when an event occurs.

Metrink is accessible via a simple RESTful API so it can be used by any language without having to install anything.

## Collecting Metrics

### What is a Metric?

A metric is a value associated with a device, group, name that is measured at a specific point in time. The device, group, and name provide a hierarchy for organizing your metrics. It is important to consider this organization before you start collecting metrics as it makes querying the metrics easier. To better explain each of these labels, we will use the example of collecting metrics from a MySQL instance using the ``show status`` command.

#### Device
The device label is the the device associated with the metric. In our MySQL example it would be the server the MySQL instance is running on; for example ``db-server``.

#### Group
The group label allows you to group metrics into logical groupings across devices. In our MySQL example you would want to put all of the metrics into the ``mysql`` group. This allows you to run a query in Metrink like ``m('db-server', 'mysql', 'Bytes_*')`` to see both the bytes sent and received. (More information on the Metrink query language can be found [here](http://www.metrink.com/documentation/query-language).) If a generic group like ``stats`` were used, then the resulting query might also pull back metrics associated with the bytes sent and received from a network interface making querying more difficult.

#### Name
The name label is simply the name given to the metric. In our MySQL monitoring example Bytes_received and Bytes_sent would be the metric names.

#### Value
The value of the metric is a number following the [JSON](http://json.org/) standard for representing numbers.

#### Timestamp
The timestamp is an optional value that represents when the metric was captured. The timestamp represents the number of milliseconds that have elapsed since 1970-01-01T00:00:00Z. If this value is not included, then Metrink will use the time it received the metric as the timestamp. For example, the timestamp value of Jan 1st 2013 at 10:30:00:00 AM UTC is ``1357054200000``.

> warning is best to be consistent with timestamps; either send all metrics with a timestamp from machines the are synchronized, or let Metrink add the timestamp to all metrics for you. **Metrics can be overwritten!**

## Sending Metrics to Metrink

Metrics can be sent from any language. They are sent to Metrink via HTTPS using the POST method to the URL provided in the [Getting Started](/integrated/documentation/getting-started) page. Metrics are encoded using JSON and **must be** compressed with gzip compression.

> warning
> Metrics should be sent at most once-a-minute. Metrink does not store metrics at a granularity smaller than once-a-minute and will aggregate any metrics sent more often using an average. **If you abuse Metrink by sending the same metric more than once-a-minute your account will be disabled.**

### JSON Schema

Metrics are encoded in JSON using the schema shown below.

    {
        "d": "device name",
        "m": [ {
            "t": "timestamp",
            "g": "group",
            "n": "name",
            "v": "value"
        } … ]
    }

This schema allows for the efficient encoding of metrics that come from the same device. If metrics associated with multiple devices needs to be sent, then these metrics must be sent in individual POST requests.

## Exceeding your Quota

If your application exceeds its quota, Metrink will continue to return the HTTP response code `200 OK`. The `Warning` header will be populated with a human readable error message indicating that some or all metrics have been discard. It is recommended that any ``Warning`` headers are always logged.

> info Proper HTTP error codes are not utilized here. This is because many applications fail to properly account for the various error codes. By returning 200 even when metrics are discarded the risk of negatively impacting an application is reduced.

## Support

All Metrink support issues or product feedback is welcome at [support@metrink.com](mailto:support@metrink.com).

