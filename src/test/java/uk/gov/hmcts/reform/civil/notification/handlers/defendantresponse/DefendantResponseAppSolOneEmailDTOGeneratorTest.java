package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefendantResponseAppSolOneEmailDTOGeneratorTest {

    private DefendantResponseAppSolOneEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        OrganisationService organisationService = mock(OrganisationService.class);
        generator = new DefendantResponseAppSolOneEmailDTOGenerator(notificationsProperties, organisationService);
    }

    @Test
    void shouldAddRespondentNameAndAllocatedTrack_OneVOneScenario() {
        // Given
        Party respondent = Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("John").individualLastName("Doe").build();
        CaseData caseData = CaseData.builder()
            .respondent1(respondent)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .build();

        // When
        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        // Then
        assertThat(result.get("respondentName")).isEqualTo("John Doe");
        assertThat(result.get("allocatedTrack")).isEqualTo("Fast Track");
    }

    @Test
    void shouldAddCombinedRespondentNames_OneVTwoScenario() {
        // Given
        Party respondent1 = Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("Alice").individualLastName("Smith").build();
        Party respondent2 = Party.builder().type(Party.Type.COMPANY).companyName("Beta Ltd").build();

        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .build();

        // When
        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        // Then
        assertThat(result.get("respondentName")).isEqualTo("Alice Smith and Beta Ltd");
        assertThat(result.get("allocatedTrack")).isEqualTo("Multi Track");
    }

    @Test
    void shouldReturnCorrectTemplateIdFromProperties() {
        // Given
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn(expectedTemplateId);

        // When
        String result = generator.getEmailTemplateId(CaseData.builder().build());

        // Then
        assertThat(result).isEqualTo(expectedTemplateId);
        verify(notificationsProperties).getClaimantSolicitorDefendantResponseFullDefence();
    }
}
