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
import uk.gov.hmcts.reform.civil.scheduler.hearingfee.publisher.HearingFeePublisherProvider;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.function.Consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingFeeSchedulerTaskTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private HearingFeePublisherProvider hearingFeePublisherProvider;

    @Mock
    private Consumer<Long> publisher;

    @InjectMocks
    private HearingFeeSchedulerTask handler;

    @Test
    void shouldInvokePublisher_whenCaseFound() {
        long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(caseId).build();
        CaseDetails caseDetails = CaseDetails.builder().id(caseId).build();

        when(coreCaseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(hearingFeePublisherProvider.provide(caseData)).thenReturn(publisher);

        handler.accept(caseDetails);

        verify(publisher).accept(caseId);
    }
}
