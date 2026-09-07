package uk.gov.hmcts.reform.civil.service.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediationSearchServiceTest {

    @Mock
    private MediationCasesSearchService mediationCasesSearchService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private MediationSearchService service;

    @Test
    void shouldReturnCsvCasesFromNonCarmMediationSearch() {
        CaseDetails caseDetails = CaseDetails.builder().id(1L).build();
        CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();
        when(mediationCasesSearchService.getInMediationCases(false)).thenReturn(List.of(caseDetails));
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        TaskResult<CaseData> result = service.getInMediationCsv();

        assertThat(result.totalResults()).isEqualTo(1);
        assertThat(result.itemStream()).containsExactly(caseData);
        assertThat(result.isEmpty()).isFalse();
        verify(mediationCasesSearchService).getInMediationCases(false);
    }

    @Test
    void shouldReturnJsonCasesFromCarmMediationSearch() {
        CaseDetails caseDetails = CaseDetails.builder().id(2L).build();
        CaseData caseData = CaseData.builder().ccdCaseReference(2L).build();
        when(mediationCasesSearchService.getInMediationCases(true)).thenReturn(List.of(caseDetails));
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

        TaskResult<CaseData> result = service.getInMediationJson();

        assertThat(result.totalResults()).isEqualTo(1);
        assertThat(result.itemStream()).containsExactly(caseData);
        verify(mediationCasesSearchService).getInMediationCases(true);
    }
}
