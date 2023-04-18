# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.9

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

# Change to non-root privilege
USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/civil-service.jar /opt/app/

# Yarn 3 upgrade
COPY .yarn ./.yarn
COPY .yarnrc.yml ./

EXPOSE 4000
CMD [ "civil-service.jar" ]
