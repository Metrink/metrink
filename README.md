# Metrink

Metrink is a metric sink. It records metrics of any type, and then allows you to graph said metrics and apply transformation functions.

## Building

```
$ mvn package
# OR
$ mvn package -DskipTests
```

## Settings

To launch any Metrink instance, first create a settings.yml.

```yaml
# Store metrics in SQL
metrics_database: sql

## Store metrics in Cassandra
#metrics_database: cassandra

# SQL must be configured regardless of metrics_database, because it's used for user meta-data.
sql:
    # H2 Configuration
    driver: org.h2.Driver
    url: jdbc:h2:target/development.db.h2
    username: sa
    password: sa
    
    ## MySQL Configuration
    #url: jdbc:mysql://localhost/metrink
    #username: metrink
    #password: password

# Configure the initial host to connect to for locating the remaining Cassandra instances
cassandra:
    seed: localhost:9160
```

## Launching

Metrink Web:

```term
$ java -jar ./metrink-web/target/metrink-web-*.jar --settings settings.yml
```

Metrink Collector:

```term
$ java -jar ./metrink-web/target/metrink-collector-*.jar --settings settings.yml server configuration.yml
```

Metrink [Liquibase Integration](http://www.dropwizard.io/manual/migrations/):

```term
# Update the database to the latest configuration in metrink-collector/src/main/resources/migrations.xml
$ java -jar ./metrink-web/target/metrink-collector-*.jar --settings settings.yml db migrate configuration.yml

# Reset the database. (This might not erase stored procedures.)
$ java -jar ./metrink-web/target/metrink-collector-*.jar --settings settings.yml db drop-all --confirm-delete-everything configuration.yml

# Look at the current schema.
$ java -jar ./metrink-web/target/metrink-collector-*.jar --settings settings.yml db dump configuration.yml
```

General notes:

* If running from eclipse, set the working directory to `${workspace_loc:metrink}`, otherwise H2 wont share the database between projects.
* To enable debug logging, add `-DLOG_LEVEL=DEBUG` to the CLI.
* To run wicket in development mode, add `-Dwicket.configuration=development` to the CLI arguments. 
