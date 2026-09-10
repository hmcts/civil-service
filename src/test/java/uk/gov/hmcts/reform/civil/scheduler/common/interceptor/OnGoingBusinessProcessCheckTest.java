package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.scheduler.common.interceptor.CaseInterceptorAttributes.BASE_CASE_DATA;
import static uk.gov.hmcts.reform.civil.scheduler.common.interceptor.CaseInterceptorAttributes.CIVIL_CASE_DATA;
import static uk.gov.hmcts.reform.civil.scheduler.common.interceptor.CaseInterceptorAttributes.GA_CASE_DATA;

@ExtendWith(MockitoExtension.class)
class OnGoingBusinessProcessCheckTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private InterceptorChain<CaseDetails> chain;

    private OnGoingBusinessProcessCheck check;

    @BeforeEach
    void setUp() {
        check = new OnGoingBusinessProcessCheck(coreCaseDataService, caseDetailsConverter);
    }

    @Test
    void shouldCallNext_whenCaseHasNoOngoingBusinessProcess() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(CASE_TYPE).build();
        CaseData caseData = new CaseDataBuilder().build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", caseDetails);
        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        check.accept(context, chain);

        verify(chain).next(context);
        assertThat(context.getAttribute(CIVIL_CASE_DATA)).isPresent().contains(caseData);
        assertThat(context.getAttribute(BASE_CASE_DATA)).isPresent().contains(caseData);
    }

    @Test
    void shouldCallNext_whenGACaseHasNoOngoingBusinessProcess() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(GENERALAPPLICATION_CASE_TYPE).build();
        GeneralApplicationCaseData gaCaseData = new GeneralApplicationCaseData();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", caseDetails);
        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(gaCaseData);

        check.accept(context, chain);

        verify(chain).next(context);
        assertThat(context.getAttribute(GA_CASE_DATA)).isPresent().contains(gaCaseData);
        assertThat(context.getAttribute(BASE_CASE_DATA)).isPresent().contains(gaCaseData);
    }

    @Test
    void shouldThrowTaskAbortedException_whenCaseHasOngoingBusinessProcess() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(CASE_TYPE).build();
        CaseData caseData = new CaseDataBuilder()
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.STARTED))
            .build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", caseDetails);
        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> check.accept(context, chain));
        assertThat(exception.getReason()).isEqualTo("Ongoing business process");
        verify(chain, never()).next(context);
    }

    @Test
    void shouldThrowTaskAbortedException_whenGACaseHasOngoingBusinessProcess() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).caseTypeId(GENERALAPPLICATION_CASE_TYPE).build();
        GeneralApplicationCaseData gaCaseData = new GeneralApplicationCaseData();
        gaCaseData.setBusinessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.STARTED));

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", caseDetails);
        when(coreCaseDataService.getCase(123L)).thenReturn(caseDetails);
        when(caseDetailsConverter.toGeneralApplicationCaseData(caseDetails)).thenReturn(gaCaseData);

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> check.accept(context, chain));
        assertThat(exception.getReason()).isEqualTo("Ongoing business process");
        verify(chain, never()).next(context);
    }
}
