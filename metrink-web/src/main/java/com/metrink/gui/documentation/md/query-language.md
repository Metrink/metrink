# Query Language

Metrink has a very powerful graphing query language making it easy to slice and dice metrics. The basic concept behind the query language is that metrics are selected and then passed through a number of transitions before finally being sent to a graphing engine. This follows very closely to the Unix-style pipe syntax.

There are four main components to a Metrink query:

1. Time
2. Metrics
3. Operators
4. Functions

**Example**:

Say you would like to graph the following:

 - CPU load for server02 and server03
 - between 21:18 and 21:48 on Jan 1st 2013
 - the same metrics from a day before overlaid
 - the average of these values
 - on a line graph

You would use the following query: ``2013-12-03 21:18 to 21:48 m("server02", "cpu", "load", -1d) m("server03", "cpu", "load", -1d) >| avg | graph``

The grammar for a Metrink query is:

```
TIME METRICS [ [ PIPE FUNCTION ] ... ] 
TIME = [ ABSOLUTE_TIME | RELATIVE_TIME ]
ABSOLUTE_TIME = [YYYY-MM-DD] HH:MM {AM | PM} to [YYYY-MM-DD] HH:MM {AM | PM}
RELATIVE_TIME = -N {m | h | d | w}
METRIC  = m(device, group, name, [, RELATIVE_TIME])
METRICS = METRIC [ METRIC ... ]
PIPE = { "|" | ">|" }
FUNCTION = function[ (argument [[, argument] ...]) ]
```

## Time

Time is specified in one of two formats: relative and absolute.

### Relative Times

The relative time formats make it quick and easy to see metrics where the end time for the range is now. Relative time formats are simply a negative number (indicating back in time) followed by one of four time units: ``m`` for minute, ``h`` for hour, ``d`` for day, and ``w`` for week (or seven days). For example, to graph metrics "from thirty minutes ago to now", you would simply use ``-30m``.

### Absolute Times

Absolute time formats allow you to specify an explicit range between two points in time. Because specifying absolute times are more verbose, they are as flexible as possible to reduce typing. 
The dates are optional for both start and end times, and when omitted it is assumed that the current day is used. For example, if you want to graph metrics from 3 AM to 5 PM today, simply use ``3:00AM to 5:00pm``. If you want to graph metrics from 3 AM to 5 PM on Jan 10th 2013, then use ``2013-1-10 3:00 AM to 5:00 PM``. You can also interchangeably use a 12-hour and 24-hour clock.

You can also specify both date and time for both the start and end points. For example to see metrics from Jan 3rd 2013 at 3 AM to Jan 23rd 2013 at 5 PM you would use: ``2013-1-3 3:00 to 2013-1-23 17:00`` (notice the use of the 24-hour clock).  

## Metrics

After the time range has been specified, one or more metrics are selected using the ``m`` function. This function takes 3 (optionally 4) parameters: ``m(device, group, name, {relative time for overlays})``.

Any **one parameter** can specify a wild-card expression using ``*``, which represents any set of characters. (_The limit of only one parameter having a wild-card prevents accidental queries that would retrieve large groups of metrics._) Multiple metrics can be specified using consecutive ``m`` functions. For example ``m("l*", "cpu", "load")`` will find the ``load`` metric in the ``cpu`` group for all devices that start with the letter ``l``.

A fourth parameter can be supplied to specify the relative time for an overlay of the same metric(s).
For example, ``m("server1", "cpu", "load", -1d)`` will find the ``load`` metric in the group ``cpu`` for the device ``server1``, and will also get the same metrics for one day ago. The resulting graph will show two lines instead of one; overlaying the metrics from a day ago on top of the current graph. Multiple overlays can be specified by using square brackets. For example to specify an overlay of 1, 2, and 3 days ago: ``m("server1", "cpu", "load", [-1d, -2d, -3d])``.

## Pipes

Functions are connected together via two different types of pipes: ``|`` standard and ``>|`` copying.

The standard pipe lets the function transform the metric values. The copying pipe will apply the transformation
function, and also copy the metric values as well.


## Functions

Functions take zero or more arguments. Each function performs a different transformation on the metrics.

| Function | Description                                                                         |
| -------- | ----------------------------------------------------------------------------------- |
| ``avg``  | Computes the average metric value in the time/date range selected.                  |
| ``deriv``| Computes the first derivative of the metric values in the time/date range selected. |
| ``int``  | Computes the integral of the metric values in the time/date range selected.        |
| ``mavg`` | Computes the moving average of the metric values in the time/date range selected.   |
| ``max``  | Computes the maximum value of the metric values in the time/date range selected.    |
| ``min``  | Computes the minimum value of the metric values in the time/date range selected.    |
| ``mul``  | Computes the multiplication of multiple metrics in the time/date range selected.    |
| ``sum``  | Computes the sum of multiple metrics in the time/date range selected.               |

| Graphing |                                                                                     |
| -------- | ----------------------------------------------------------------------------------- |
| graph    | Graph the time-series as a line graph. _This is the default when no graphing function is specified._ |
| area     | Create a stacked area graph of the time-series.                                     |
| histo    | Create a histogram of the metric values.                                            | 


### Math Operations

To make simple operations like summation, multiplication, subtraction, and division easier, these two math operations can be performed on metrics. For example to find the ratio of bytes received to bytes sent to a MySQL server you could use the following query: ``-1h m("db1", "mysql", "bytes received") / m("db1", "mysql", "bytes sent")``.

> You cannot use wild-cards when specifying math operations.








