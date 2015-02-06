# Alerting and Actions

Metrink has a flexible alerting infrastructure the leverages the Metrink query language. Alerts are described by a query that satisfies a condition for a portion of time. When the condition is statisfied, then an action is performed. Actions can either be emails or SMS messages sent to a cell phone. Actions are named so they can easily be referenced by multiple actions.

**Example**:

Say you would like to be paged when the CPU load for dbserver is over 90 for more than 5 minutes. You would construct the following alert, ``m("dbserver", "cpu", "load") > 90 for 5m do page_me``. You would also need to define the ``page_me`` action.

The grammar for a Metrink alert is:

```
METRIC CONDITION [for RELATIVE_TIME] do ACTION
METRIC  = m(device, group, name)
CONDITION = {> | < | >= | <= | ==} N
RELATIVE_TIME = N {m | h | d | w}
ACTION = a defined action name
```

## Metrics

While the metric function of an alert query follows very closely with the metric function of a graph query, there are a few limitations:

- Metric functions cannot have overlays
- Metric functions cannot have more than one wildcard
- There can be only one metric function
- You cannot uses pipes or math functions

## Conditions

A condition is a comparison operator and a value. The comparison operators are the familiar mathematical comparisons and the equal operator found in most programming languages. A numeric value follows the comparision operator. The condition must be met, including the time duration, for the alert to trigger.

## Relative Time

Unlike graph queries where relative times are negative, relative times in alert queries must be positive. This is because the represent an amount of time for which the condition must be true to trigger the action. The relative time units in an alert query are the same as those in a graph query: ``m`` for minute, ``h`` for hour, ``d`` for day, and ``w`` for week (or seven days).

## Actions

Actions are referenced by name making it easier to reference the same action for multiple alerts. Besides the name, an action has a type and a value. Metrink supports the following types of actions:

- Email: an email is sent to the address specified
- AT&T SMS: an SMS (text) message is sent to an AT&T cell phone
- Sprint SMS: an SMS (text) message is sent to a Sprint cell phone
- T-Mobile SMS: an SMS (text) message is sent to a T-Mobile cell phone
- Verizon SMS: an SMS (text) message is sent to a Verizon cell phone

It is important to select the proper carrier when selecting an SMS action. This is because the way text messages are sent to each carrier are done differently.