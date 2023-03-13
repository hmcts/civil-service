package uk.gov.hmcts.reform.civil.service.hearings;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.SneakyThrows;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class HearingValuesServiceTest {

    @Mock
    private CoreCaseDataService caseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @InjectMocks
    private HearingValuesService hearingValuesService;

    @Test
    void shouldReturnExpectedHearingValuesWhenCaseDataIsReturned() {
        var caseId = 1L;
        var caseData = CaseData.builder().ccdCaseReference(caseId).build();
        var caseDetails = CaseDetails.builder().id(caseId).data(new HashMap<>()).build();
        var expected = ServiceHearingValuesModel.builder().caseSLAStartDate("2023-01-30").build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        when(deadlinesCalculator.getSlaStartDate(caseData)).thenReturn(LocalDate.of(2023, 1, 30));

        var actual = hearingValuesService.getValues(caseId, "8AB87C89");

        verify(caseDetailsConverter).toCaseData(eq(caseDetails.getData()));
        verify(deadlinesCalculator).getSlaStartDate(eq(caseData));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SneakyThrows
    void shouldReturnExpectedHearingValuesWhenCaseDataIs() {
        var caseId = 1L;

        doThrow(new NotFoundException("", new RestException("", new Exception())))
            .when(caseDataService).getCase(caseId);

        assertThrows(
            CaseNotFoundException.class,
            () -> hearingValuesService.getValues(caseId, "8AB87C89"));
    }
}

