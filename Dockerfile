ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

# Change to non-root privilege
USER hmcts

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/civil-service.jar /opt/app/

EXPOSE 4000
CMD [ "civil-service.jar" ]
