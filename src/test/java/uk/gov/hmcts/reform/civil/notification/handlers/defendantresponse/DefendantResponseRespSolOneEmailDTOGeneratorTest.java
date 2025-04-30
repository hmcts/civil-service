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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ALLOCATED_TRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

class DefendantResponseRespSolOneEmailDTOGeneratorTest {

    private DefendantResponseRespSolOneEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        OrganisationService organisationService = mock(OrganisationService.class);
        generator = new DefendantResponseRespSolOneEmailDTOGenerator(notificationsProperties, organisationService);
    }

    @Test
    void shouldReturnCorrectEmailTemplateId() {
        String expectedTemplateId = "some-template-id";
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn(expectedTemplateId);

        String actualTemplateId = generator.getEmailTemplateId(CaseData.builder().build());

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("defendant-response-applicant-notification-%s");
    }

    @Test
    void shouldAddSingleRespondentNameAndTrack() {
        Party respondent = Party.builder()
            .individualFirstName("John")
            .individualLastName("Doe")
            .type(Party.Type.INDIVIDUAL)
            .build();

        CaseData caseData = CaseData.builder()
            .respondent1(respondent)
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(RESPONDENT_NAME)).isEqualTo("John Doe");
        assertThat(result.get(ALLOCATED_TRACK)).isEqualTo("Small Claim Track");
    }

    @Test
    void shouldAddCombinedRespondentNamesWhenMultipleDefendants() {
        Party respondent1 = Party.builder()
            .individualFirstName("Alice")
            .individualLastName("Brown")
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .companyName("Beta Ltd")
            .type(Party.Type.COMPANY)
            .build();

        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .build();

        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(result.get(RESPONDENT_NAME)).isEqualTo("Alice Brown and Beta Ltd");
        assertThat(result.get(ALLOCATED_TRACK)).isEqualTo("Multi Track");
    }

    @Test
    void shouldReturnCorrectTemplateIdFromProperties() {
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn(expectedTemplateId);

        String result = generator.getEmailTemplateId(CaseData.builder().build());

        assertThat(result).isEqualTo(expectedTemplateId);
        verify(notificationsProperties).getClaimantSolicitorDefendantResponseFullDefence();
    }
}
