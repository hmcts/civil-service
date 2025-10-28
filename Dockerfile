# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.3

# Application image

FROM hmctspublic.azurecr.io/imported/distroless/java25

# Change to non-root privilege
USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/civil-service.jar /opt/app/

EXPOSE 4000
CMD [ "civil-service.jar" ]
