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
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class HearingValuesServiceTest {

    @Mock
    private CoreCaseDataService caseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private HearingValuesService hearingValuesService;

    @Test
    void shouldReturnExpectedHearingValuesWhenCaseDataIsReturned() {
        var caseId = 1L;
        var caseDetails = CaseDetails.builder().id(caseId).build();
        var caseData = CaseData.builder()
            .ccdCaseReference(caseId)
            .applicant1(Party.builder()
                            .individualFirstName("Applicant")
                            .individualLastName("One")
                            .type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder()
                             .individualFirstName("Respondent")
                             .individualLastName("One")
                             .type(Party.Type.INDIVIDUAL).build())
            .build();
        var expected = ServiceHearingValuesModel.builder()
            .publicCaseName("'Applicant One' v 'Respondent One'")
            .build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);

        var actual = hearingValuesService.getValues(caseId, "8AB87C89");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SneakyThrows
    void shouldReturnExpectedHearingValuesWhenCaseDateIsNotFound() {
        var caseId = 1L;

        doThrow(new NotFoundException("", new RestException("", new Exception())))
            .when(caseDataService).getCase(caseId);

        assertThrows(
            CaseNotFoundException.class,
            () -> hearingValuesService.getValues(caseId, "8AB87C89"));
    }
}

