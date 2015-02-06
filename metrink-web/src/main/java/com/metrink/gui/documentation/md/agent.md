# Metrink Agent

The Metrink Agent makes it very easy to collect metrics about your system. It can easily be extended by adding any script to the ``scripts`` directory that simply outputs metrics to standard out.

## Requirements

* Linux, kernel version 3.0 or greater
* Network access to https://metrink.com/
* Python3

## Setup

Configuring the Metrink Agent is very easy, just follow these steps:

1. Download the Metrink Agent: ``wget -q https://storage.googleapis.com/metrink-gae/metrink-agent.tar.gz``
1. Extract the agent: ``tar xzf metrink-agent.tar.gz``
1. Change into the agent directory: ``cd metrink-agent``
1. Run the setup script: ``./setup.sh``
1. Follow the on-screen directions

## Adding New Metrics
The Metrink Agent gathers metrics by running scripts that read information about the system and printing the results to standard out. Results are comma delineated with the following fields:

```
host,group,name,value
```

The scripts can be written in any language just so long as they can be executed directly without any command line parameters. For example, if you can type in ``./your-script`` and your script runs, then the agent can use the script.
 
If you need additional support configuring the Metrink Agent, please contact [support@metrink.com](mailto:support@metrink.com).

