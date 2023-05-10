# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.12

# Application image
FROM hmctspublic.azurecr.io/base/java:17-distroless
LABEL maintainer="https://github.com/hmcts/civil-service"

# Change to non-root privilege
USER hmcts
COPY lib/AI-Agent.xml /opt/app/
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/civil-service.jar /opt/app/

EXPOSE 4000
CMD [ "civil-service.jar" ]
