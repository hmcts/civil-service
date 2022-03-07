ARG APP_INSIGHTS_AGENT_VERSION=2.5.1

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.2

# Add a new user "user_pantheon_ac" with user id 8877
RUN useradd -u 8877 civil_user

# Change to non-root privilege
USER civil_user

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/civil-service.jar /opt/app/

EXPOSE 4000
CMD [ "civil-service.jar" ]
