###############################
# Phoenix Finance (Multi-stage)
###############################
# Stage 1: Build the WAR using Maven on JDK 25
FROM eclipse-temurin:25-jdk-noble AS build

# Install Maven 3.9.9
ARG MAVEN_VERSION=3.9.9
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates && \
    curl -fsSL https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
      -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven && \
    rm /tmp/maven.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

WORKDIR /build

# Only copy pom first for dependency caching
COPY phoenix_investment_finance/pom.xml phoenix_investment_finance/pom.xml
RUN mvn -f phoenix_investment_finance/pom.xml -DskipTests -DskipWildFlyDeploy=true dependency:resolve dependency:resolve-plugins

# Copy sources and build
COPY phoenix_investment_finance/src phoenix_investment_finance/src
COPY wildfly-module wildfly-module
RUN mvn -f phoenix_investment_finance/pom.xml -DskipTests -DskipWildFlyDeploy=true clean package

# Stage 2: Runtime — WildFly 39.0.1.Final on JDK 25
FROM eclipse-temurin:25-jdk-noble

ARG WILDFLY_VERSION=39.0.1.Final
ENV JBOSS_HOME=/opt/jboss/wildfly

# Create jboss group and user (same convention as official WildFly images)
RUN groupadd -r jboss -g 1100 && \
    useradd -u 1100 -r -g jboss -m -d /opt/jboss -s /bin/bash jboss

# Download and install WildFly
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates && \
    curl -fsSL https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz \
      -o /tmp/wildfly.tar.gz && \
    tar -xzf /tmp/wildfly.tar.gz -C /opt/jboss && \
    mv /opt/jboss/wildfly-${WILDFLY_VERSION} ${JBOSS_HOME} && \
    rm /tmp/wildfly.tar.gz && \
    chown -R jboss:jboss /opt/jboss && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Environment variables consumed by datasource descriptor phoenix_investment_finance-ds.xml
ENV POSTGRES_HOST=postgres \
    POSTGRES_PORT=5432 \
    POSTGRES_DB=phoenixdb \
    POSTGRES_USER=postgres \
    POSTGRES_PASSWORD=postgres

# Install PostgreSQL JDBC driver as WildFly module
RUN mkdir -p ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main
ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.7/postgresql-42.7.7.jar ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/postgresql-42.7.7.jar
COPY wildfly-module/org/postgresql/main/module.xml ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/module.xml
RUN chown -R jboss:jboss ${JBOSS_HOME}/modules/system/layers/base/org/postgresql

# Copy startup script that will register driver and start WildFly
COPY scripts/configure-wildfly.sh ${JBOSS_HOME}/bin/configure-wildfly.sh
RUN chmod +x ${JBOSS_HOME}/bin/configure-wildfly.sh && chown jboss:jboss ${JBOSS_HOME}/bin/configure-wildfly.sh

USER jboss

# Copy built WAR from build stage
COPY --from=build /build/phoenix_investment_finance/target/phoenix_investment_finance.war ${JBOSS_HOME}/standalone/deployments/phoenix_investment_finance.war

EXPOSE 8080 9990
CMD ["/opt/jboss/wildfly/bin/configure-wildfly.sh"]
