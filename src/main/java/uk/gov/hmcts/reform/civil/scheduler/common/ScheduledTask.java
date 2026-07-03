package uk.gov.hmcts.reform.civil.scheduler.common;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.function.Consumer;

public interface ScheduledTask extends Consumer<CaseDetails> {

    default long maxCasesPerRun() {
        return Long.MAX_VALUE;
    }

    default ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return ScheduledTaskBackPressureConfiguration.disabled();
    }
}
