package uk.gov.hmcts.reform.civil.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class FeatureToggleService {

    private final LDClientInterface internalClient;
    private final String environment;

    private final Environment systemEnvironment;

    @Autowired
    public FeatureToggleService(LDClientInterface internalClient,
                                @Value("${launchdarkly.env}") String environment,
                                Environment systemEnvironment) {
        this.internalClient = internalClient;
        this.environment = environment;
        this.systemEnvironment = systemEnvironment;
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, createLDUser().build(), false);
    }

    public boolean isFeatureEnabled(String feature, LDUser user) {
        return internalClient.boolVariation(feature, user, false);
    }

    public boolean isOrganisationOnboarded(String orgId) {
        LDUser ldUser = createLDUser().custom("orgId", orgId).build();
        return internalClient.boolVariation("isOrganisationOnboarded", ldUser, false);
    }

    public boolean isRpaContinuousFeedEnabled() {
        return internalClient.boolVariation("rpaContinuousFeed", createLDUser().build(), false);
    }

    public boolean isSpecRpaContinuousFeedEnabled() {
        return internalClient.boolVariation(
            "specified-rpa-continuous-feed",
            createLDUser().build(),
            false
        );
    }

    public boolean isGlobalSearchEnabled() {
        return internalClient.boolVariation(
            "global-search-specified",
            createLDUser().build(),
            false
        );
    }

    public boolean isLrSpecEnabled() {
        return isFeatureEnabled("specified-lr-journey");
    }

    public boolean isSdoEnabled() {
        return isFeatureEnabled("enableSDO");
    }

    public boolean isGeneralApplicationsEnabled() {
        String runningEnv = systemEnvironment.getProperty("ENVIRONMENT");
        List<String> gaSupportedEnvs = List.of("preview", "demo");
        if (gaSupportedEnvs.contains(runningEnv)) {
            return internalClient.boolVariation("general_applications_enabled", createLDUser().build(), false);
        } else {
            return false;
        }
    }

    public LDUser.Builder createLDUser() {
        return new LDUser.Builder("civil-service")
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", environment);
    }

    public boolean isNoticeOfChangeEnabled() {
        return internalClient.boolVariation("notice-of-change", createLDUser().build(), false);
    }

    public boolean isHearingAndListingSDOEnabled() {
        return internalClient.boolVariation("hearing-and-listing-sdo", createLDUser().build(), false);
    }

    public boolean isCourtLocationDynamicListEnabled() {
        return internalClient.boolVariation("court-location-dynamic-list", createLDUser().build(), false);
    }

    public boolean isPinInPostEnabled() {
        return internalClient.boolVariation("pin-in-post", createLDUser().build(), false);
    }

    public boolean isAccessProfilesEnabled() {
        return internalClient.boolVariation("access-profiles", createLDUser().build(), false);
    }

    public boolean isSDOEnabled() {
        return internalClient.boolVariation("enableSDO", createLDUser().build(), false);
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            log.error("Error in closing the Launchdarkly client::", e);
        }
    }
}
