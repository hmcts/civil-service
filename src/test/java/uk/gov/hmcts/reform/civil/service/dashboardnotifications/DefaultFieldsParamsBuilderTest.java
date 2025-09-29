package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultFieldsParamsBuilderTest {

    private DefaultFieldsParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DefaultFieldsParamsBuilder();
    }

    @Test
    void shouldAddDefaultFieldsToParams() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        Party applicant1 = mock(Party.class);

        when(caseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(caseData.getLegacyCaseReference()).thenReturn("LEGACY123");
        when(respondent1.getPartyName()).thenReturn("Respondent Name");
        when(applicant1.getPartyName()).thenReturn("Applicant Name");
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(caseData.getApplicant1()).thenReturn(applicant1);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("ccdCaseReference", 1234567890123456L);
        assertThat(params).containsEntry("legacyCaseReference", "LEGACY123");
        assertThat(params).containsEntry("defaultRespondTime", "4pm");
        assertThat(params).containsEntry("respondent1PartyName", "Respondent Name");
        assertThat(params).containsEntry("applicant1PartyName", "Applicant Name");
        assertThat(params).containsKey("priorityNotificationDeadline");
        assertThat(params.get("priorityNotificationDeadline")).isInstanceOf(LocalDateTime.class);
    }
}
