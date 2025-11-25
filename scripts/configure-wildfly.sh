#!/bin/bash
# Configure WildFly for PostgreSQL before starting

# Start WildFly in admin-only mode
/opt/jboss/wildfly/bin/standalone.sh --admin-only &
WILDFLY_PID=$!

# Wait for WildFly to be ready
until `/opt/jboss/wildfly/bin/jboss-cli.sh -c ":read-attribute(name=server-state)" 2> /dev/null | grep -q running`; do
  sleep 1
done

# Register PostgreSQL driver
/opt/jboss/wildfly/bin/jboss-cli.sh --connect --command="/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver)"

# Shutdown admin-only instance
/opt/jboss/wildfly/bin/jboss-cli.sh --connect --command=":shutdown"
wait $WILDFLY_PID

# Start WildFly normally
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0
