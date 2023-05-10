# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.9

# Application image
FROM hmctspublic.azurecr.io/base/java:17-distroless-1.4
LABEL maintainer="https://github.com/hmcts/civil-service"

# Change to non-root privilege
USER hmcts
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/civil-service.jar /opt/app/

EXPOSE 4000
CMD [ "civil-service.jar" ]
