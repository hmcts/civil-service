package uk.gov.hmcts.reform.civil.launchdarkly;

import com.launchdarkly.sdk.EvaluationDetail;
import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.FeatureFlagsState;
import com.launchdarkly.sdk.server.FlagsStateOption;
import com.launchdarkly.sdk.server.interfaces.BigSegmentStoreStatusProvider;
import com.launchdarkly.sdk.server.interfaces.DataSourceStatusProvider;
import com.launchdarkly.sdk.server.interfaces.DataStoreStatusProvider;
import com.launchdarkly.sdk.server.interfaces.FlagChangeListener;
import com.launchdarkly.sdk.server.interfaces.FlagTracker;
import com.launchdarkly.sdk.server.interfaces.FlagValueChangeListener;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Simple LDClientInterface implementation that always returns the provided default values.
 * Used when LaunchDarkly is not configured (e.g. in tests/local dev) so that the SDK threads
 * are never started.
 */
public class NoopLaunchDarklyClient implements LDClientInterface {

    private static final FeatureFlagsState EMPTY_FLAG_STATE = FeatureFlagsState.builder().build();
    private static final FlagChangeListener NOOP_FLAG_CHANGE_LISTENER = event -> { };
    private static final FlagTracker NOOP_FLAG_TRACKER = new FlagTracker() {
        @Override
        public void addFlagChangeListener(FlagChangeListener listener) {
            // no-op
        }

        @Override
        public void removeFlagChangeListener(FlagChangeListener listener) {
            // no-op
        }

        @Override
        public FlagChangeListener addFlagValueChangeListener(String flagKey,
                                                             LDContext context,
                                                             FlagValueChangeListener listener) {
            return NOOP_FLAG_CHANGE_LISTENER;
        }
    };
    private static final BigSegmentStoreStatusProvider NOOP_BIG_SEGMENT_STATUS = new BigSegmentStoreStatusProvider() {

        private final Status status = new Status(false, false);

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public void addStatusListener(StatusListener listener) {
            // no-op
        }

        @Override
        public void removeStatusListener(StatusListener listener) {
            // no-op
        }
    };
    private static final DataSourceStatusProvider NOOP_DATA_SOURCE_STATUS = new DataSourceStatusProvider() {

        private final Status status = new Status(State.OFF, Instant.EPOCH, null);

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public void addStatusListener(StatusListener listener) {
            // no-op
        }

        @Override
        public void removeStatusListener(StatusListener listener) {
            // no-op
        }

        @Override
        public boolean waitFor(State desiredState, Duration timeout) {
            return desiredState == State.OFF;
        }
    };
    private static final DataStoreStatusProvider NOOP_DATA_STORE_STATUS = new DataStoreStatusProvider() {

        private final Status status = new Status(true, false);
        private final CacheStats cacheStats = new CacheStats(0, 0, 0, 0, 0, 0);

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public void addStatusListener(StatusListener listener) {
            // no-op
        }

        @Override
        public void removeStatusListener(StatusListener listener) {
            // no-op
        }

        @Override
        public CacheStats getCacheStats() {
            return cacheStats;
        }
    };

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void track(String eventName, LDContext context) {
        // no-op
    }

    @Override
    public void trackData(String eventName, LDContext context, LDValue data) {
        // no-op
    }

    @Override
    public void trackMetric(String eventName, LDContext context, LDValue data, double metricValue) {
        // no-op
    }

    @Override
    public void identify(LDContext context) {
        // no-op
    }

    @Override
    public FeatureFlagsState allFlagsState(LDContext context, FlagsStateOption... options) {
        return EMPTY_FLAG_STATE;
    }

    @Override
    public boolean boolVariation(String key, LDContext context, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public int intVariation(String key, LDContext context, int defaultValue) {
        return defaultValue;
    }

    @Override
    public double doubleVariation(String key, LDContext context, double defaultValue) {
        return defaultValue;
    }

    @Override
    public String stringVariation(String key, LDContext context, String defaultValue) {
        return defaultValue;
    }

    @Override
    public LDValue jsonValueVariation(String key, LDContext context, LDValue defaultValue) {
        return defaultValue;
    }

    @Override
    public EvaluationDetail<Boolean> boolVariationDetail(String key, LDContext context, boolean defaultValue) {
        return null;
    }

    @Override
    public EvaluationDetail<Integer> intVariationDetail(String key, LDContext context, int defaultValue) {
        return null;
    }

    @Override
    public EvaluationDetail<Double> doubleVariationDetail(String key, LDContext context, double defaultValue) {
        return null;
    }

    @Override
    public EvaluationDetail<String> stringVariationDetail(String key, LDContext context, String defaultValue) {
        return null;
    }

    @Override
    public EvaluationDetail<LDValue> jsonValueVariationDetail(String key, LDContext context, LDValue defaultValue) {
        return null;
    }

    @Override
    public boolean isFlagKnown(String featureKey) {
        return false;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void flush() {
        // no-op
    }

    @Override
    public boolean isOffline() {
        return true;
    }

    @Override
    public FlagTracker getFlagTracker() {
        return NOOP_FLAG_TRACKER;
    }

    @Override
    public BigSegmentStoreStatusProvider getBigSegmentStoreStatusProvider() {
        return NOOP_BIG_SEGMENT_STATUS;
    }

    @Override
    public DataSourceStatusProvider getDataSourceStatusProvider() {
        return NOOP_DATA_SOURCE_STATUS;
    }

    @Override
    public DataStoreStatusProvider getDataStoreStatusProvider() {
        return NOOP_DATA_STORE_STATUS;
    }

    @Override
    public String secureModeHash(LDContext context) {
        return null;
    }

    @Override
    public String version() {
        return "noop";
    }
}
