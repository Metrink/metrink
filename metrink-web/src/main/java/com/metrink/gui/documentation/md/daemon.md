# Daemon

The Metrink Daemon is ideal for engineers who want to focus on administering their systems, not administering
their monitoring solution.
Metrink uses an agentless deployment strategy. You only need to install the Metrink Daemon on to a single host and then utilizing standard technologies the Metrink Daemon will gather metrics from all the devices in your infrastructure.
The Metrink Daemon is highly configurable, making it easy to record custom metrics for devices.
In most environments, configuration is as simple as listing the IP address (or ranges) of machines you would like to monitor

If you haven't already done so, obtain your API URL from the [Getting Started](/integrated/documentation/getting-started) page.

## Requirements

* Linux, kernel version 3.0 or greater
* Java 1.6 or greater
* Network access to https://metrink.com/
* SSH Server
* Windows beta agent available

## Download

[Latest Version](https://storage.googleapis.com/metrink-gae/metrink-daemon.tar.gz)

## Setup

The Metrink Daemon gathers metrics by SSHing into the target host and running a series of commands. 

> info The daemon has "targets". Targets are the hosts from which we will gather metrics.

### Daemon Setup

If you do not already have passwordless SSH authentication configured,
create a SSH public/private key pair without entering a passphrase:

```term
$ ssh-keygen
Enter file in which to save the key (/home/metrink/.ssh/id_rsa):
Enter passphrase (empty for no passphrase): 
Enter same passphrase again: 
Your identification has been saved in /home/metrink/.ssh/id_rsa.
Your public key has been saved in /home/metrink/.ssh/id_rsa.pub.
```

Once passwordless SSH is configured, download and extract the latest release of the Metrink daemon:

```term
$ mkdir metrink_daemon
$ cd metrink_daemon

$ wget https://storage.googleapis.com/metrink-gae/metrink-daemon.tar.gz
$ tar -xzf metrink-daemon.tar.gz
```

Copy the distribution YAML configuration and edit it:

```term
$ cp configuration.yml-dist configuration.yml
$ vim configuration.yml

url: <Your API URL>
...
targets:
    - "my-test-host.example.org"
...
```

With the Metrink Daemon configured, you can launch it and being collecting metrics:

```term
$ ./metrink.sh
```

Additionally, when modifying the `configuration.yml`, be sure to account for the following:

* If your key is not found at `~/.ssh/id_rsa`, change `defaults : ssh : private_key`
* If your target SSH account is not `metrink`, change `defaults : ssh : user`

To terminate the daemon:

```
$ kill `cat runner.pid`
```

### Target Setup

If your targets are not already configured for passwordless authentication, run the following on each host: 

> warning Replace <id_rsa.pub> with the contents of ~/.ssh/id_rsa.pub from the daemon server.

```term
$ mkdir -p .ssh
$ touch .ssh/authorized_keys
$ chmod 700 .ssh
$ chmod 600 .ssh/authorized_keys
$ tee -a .ssh/authorized_keys <<EOF
<id_rsa.pub>
EOF
```

## If Something Goes Wrong
While the Metrink Daemon usually works the first time without any issues, sometimes problems do arise. Most of these issues are minor configuration problems and can be solved by looking at the logs and tweaking a configuration. The logs for the Metrink Daemon can be found in the ``metrink_daemon`` directory.

The Metrink Daemon is continuously monitored (and restarted if needed) by a program called ``runner``. The logs for ``runner`` can be found in ``runner.log``. It is normal to see ``runner`` occasionally restart the Metrink Daemon process.

If you ever need additional support configuring the Metrink Daemon, please contact [support@metrink.com](mailto:support@metrink.com).

## How Does it Work?
The Metrink Daemon works by SSHing into your target hosts and running various scripts. These scripts poll the system for metrics including: CPU usage, memory usage, disk usage, and network usage. The metrics are then compressed and sent to Metrink via the (http://localhost:8080/documentation/api/)[Metrink API]. You can write your own custom scripts and enable them in the Metrink Daemon making it easy to collect custom metrics without having to deal directly with the Metrink API. 
