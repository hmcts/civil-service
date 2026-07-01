package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MediationFileTransferScheduledTaskTest {

    private static final long CASE_ID = 123L;

    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    private MediationFileTransferScheduledTask task;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        task = new MediationFileTransferScheduledTask(coreCaseDataService, defaultBackPressureConfiguration);
        caseData = CaseData.builder().ccdCaseReference(CASE_ID).build();
    }

    @Test
    void shouldReturnCaseId() {
        assertThat(task.getItemId(caseData)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldMarkCaseSent() {
        task.accept(caseData);

        verify(coreCaseDataService).triggerEvent(
            eq(CASE_ID),
            eq(CaseEvent.UPDATE_CASE_DATA),
            eq(Map.of("mediationFileSentToMmt", YesOrNo.YES)),
            any(),
            any()
        );
    }
}
