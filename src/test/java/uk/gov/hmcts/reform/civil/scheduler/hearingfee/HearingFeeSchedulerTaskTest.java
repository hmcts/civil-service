package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.HearingFeePublisherProvider;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingFeeSchedulerTaskTest {

    private static final long CASE_ID = 123L;

    @Mock
    private DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Mock
    private ScheduledTaskBackPressureConfiguration backPressureConfiguration;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private HearingFeePublisherProvider hearingFeePublisherProvider;

    @Mock
    private Consumer<Long> publisher;

    @InjectMocks
    private HearingFeeSchedulerTask task;

    @Test
    void shouldReturnCaseId() {
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();
        assertThat(task.getItemId(caseDetails)).isEqualTo(CASE_ID);
    }

    @Test
    void shouldUseDefaultBackPressureConfiguration() {
        when(defaultBackPressureConfiguration.getDefaultBackPressure()).thenReturn(backPressureConfiguration);

        assertThat(task.backPressureConfiguration()).isSameAs(backPressureConfiguration);
    }

    @Test
    void shouldInvokePublisher_whenCaseFound() {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(CASE_ID).build();
        CaseDetails caseDetails = CaseDetails.builder().id(CASE_ID).build();

        when(coreCaseDataService.getCase(CASE_ID)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(hearingFeePublisherProvider.provide(caseData)).thenReturn(publisher);

        task.accept(caseDetails);

        verify(publisher).accept(CASE_ID);
    }
}
