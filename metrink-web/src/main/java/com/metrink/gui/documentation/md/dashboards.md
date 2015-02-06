# Dashboards

Metrink's dashboards allow you to easily see any aspect of your infrastructure. You can view multiple devices on the same graph, and multiple graphs on the same page. Best of all, configuring a dashboard is as easy as copy and pasting a query. You can even save a query directly to a new dashboard.

## Creating a New Dashboard

A new dashboard can be created directly from the [graphing](/graphing) page. Simply enter your query and press the "Dashboard" button to save the query to a new dashboard. A dashboard can also be created directly from the dashboard page by clicking the "Create Dashbaord" button.

## Configuring a Dashboard

A dashboard is composed of two elements: title and definition. The title names your dashbaord so you can easily find it again for display. The definition is a list of graphs you would like to see on your dashboard. The definition follows the YAML syntax.

The definition starts with the ``-row:`` directive to indicate you would like a new row of graphs in your dashboard. Following that directive, you specify the graph query you would like to run prefixed with ``- graph:``. You can specify as many graphs per row as you would like, but more than about 4 and most monitors will be too small to show useful graphs.

**Example**

```
- row:
  - graph: -30m m("*", "cpu", "load")
  - graph: -30m m(["metrink-ny2-01", "metrink-ny2-02"], "net", "eth0*")
- row:
  - graph: -30m m("*", "memory", "used memory")
  - graph: -1d m(["metrink-ny2-01", "metrink-ny2-02"], "disk", "*read rate") m(["metrink-ny2-01", "metrink-ny2-02"], "disk", "*write rate")
```

In the example above, we have constructed a dashboard with 4 graphs, 2 per row. The top two graphs show the CPU across all machines and the read and write rate of the eth0 interface. The second row shows the memory used across all machines and the disk read and write rates.

