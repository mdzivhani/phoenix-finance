###############################
# Phoenix Finance (Multi-stage)
###############################
# Stage 1: Build the WAR using Maven inside container (no local Maven required)
FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /build

# Only copy pom first for dependency caching
COPY phoenix_investment_finance/pom.xml phoenix_investment_finance/pom.xml
RUN mvn -f phoenix_investment_finance/pom.xml -DskipTests -DskipWildFlyDeploy=true dependency:resolve dependency:resolve-plugins

# Copy sources and build
COPY phoenix_investment_finance/src phoenix_investment_finance/src
COPY wildfly-module wildfly-module
RUN mvn -f phoenix_investment_finance/pom.xml -DskipTests -DskipWildFlyDeploy=true clean package

# Stage 2: Runtime WildFly image with PostgreSQL module and deployed app
FROM jboss/wildfly:latest

# Environment variables consumed by datasource descriptor phoenix_investment_finance-ds.xml
ENV POSTGRES_HOST=postgres \
    POSTGRES_PORT=5432 \
    POSTGRES_DB=phoenixdb \
    POSTGRES_USER=postgres \
    POSTGRES_PASSWORD=postgres

# Install PostgreSQL JDBC driver as WildFly module (driver jar fetched directly from Maven Central)
USER root
RUN mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.5/postgresql-42.7.5.jar /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/postgresql-42.7.5.jar
COPY wildfly-module/org/postgresql/main/module.xml /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/main/module.xml
RUN chown -R jboss:jboss /opt/jboss/wildfly/modules/system/layers/base/org/postgresql

# Copy startup script that will register driver and start WildFly
COPY scripts/configure-wildfly.sh /opt/jboss/wildfly/bin/configure-wildfly.sh
RUN chmod +x /opt/jboss/wildfly/bin/configure-wildfly.sh && chown jboss:jboss /opt/jboss/wildfly/bin/configure-wildfly.sh
USER jboss

# Copy built WAR from build stage
COPY --from=build /build/phoenix_investment_finance/target/phoenix_investment_finance.war /opt/jboss/wildfly/standalone/deployments/phoenix_investment_finance.war

EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/configure-wildfly.sh"]
