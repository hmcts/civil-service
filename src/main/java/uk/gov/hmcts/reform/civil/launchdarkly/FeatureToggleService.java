package uk.gov.hmcts.reform.civil.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FeatureToggleService {

    private final LDClientInterface internalClient;
    private final String environment;

    @Autowired
    public FeatureToggleService(LDClientInterface internalClient, @Value("${launchdarkly.env}") String environment) {
        this.internalClient = internalClient;
        this.environment = environment;
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, createLDUser().build(), false);
    }

    public boolean isFeatureEnabled(String feature, LDUser user) {
        return internalClient.boolVariation(feature, user, false);
    }

    public boolean isRpaContinuousFeedEnabled() {
        return true;
    }

    public boolean isSpecRpaContinuousFeedEnabled() {
        return true;
    }

    public boolean isGlobalSearchEnabled() {
        return true;
    }

    public boolean isSdoEnabled() {
        return true;
    }

    public boolean isGeneralApplicationsEnabled() {
        return  false;
    }

    public LDUser.Builder createLDUser() {
        return new LDUser.Builder("civil-service")
            .custom("timestamp", String.valueOf(System.currentTimeMillis()))
            .custom("environment", environment);
    }

    public boolean isNoticeOfChangeEnabled() {
        return false;
    }

    public boolean isHearingAndListingSDOEnabled() {
        return false;
    }

    public boolean isHearingAndListingLegalRepEnabled() {
        return true;
    }

    public boolean isCourtLocationDynamicListEnabled() {
        return true;
    }

    public boolean isCaseFlagsEnabled() {
        return false;
    }

    public boolean isPinInPostEnabled() {
        return false;
    }

    public boolean isPbaV3Enabled() {
        return false;
    }

    public boolean isSDOEnabled() {
        return true;
    }

    public boolean isCertificateOfServiceEnabled() {
        return false;
    }

    public boolean isHmcEnabled() {
        return false;
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            log.error("Error in closing the Launchdarkly client::", e);
        }
    }
}
