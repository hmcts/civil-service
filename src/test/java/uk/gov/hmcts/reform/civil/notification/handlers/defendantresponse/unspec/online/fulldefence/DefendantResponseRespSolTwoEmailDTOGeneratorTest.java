package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.online.fulldefence;

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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ALLOCATED_TRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;

class DefendantResponseRespSolTwoEmailDTOGeneratorTest {

    private DefendantResponseRespSolTwoEmailDTOGenerator generator;
    private NotificationsProperties notificationsProperties;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        OrganisationService organisationService = mock(OrganisationService.class);
        generator = new DefendantResponseRespSolTwoEmailDTOGenerator(notificationsProperties, organisationService);
    }

    @Test
    void shouldReturnCorrectTemplateId() {
        String expectedTemplateId = "sol-litigation-friend-template";
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn(expectedTemplateId);

        String actualTemplateId = generator.getEmailTemplateId(CaseData.builder().build());

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("defendant-response-applicant-notification-%s");
    }

    @Test
    void shouldAddRespondentNamesAndTrackToProperties() {
        Party respondent1 = Party.builder()
            .individualFirstName("Alice")
            .individualLastName("Smith")
            .type(Party.Type.INDIVIDUAL)
            .build();

        Party respondent2 = Party.builder()
            .companyName("Beta Corp")
            .type(Party.Type.COMPANY)
            .build();

        CaseData caseData = CaseData.builder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .build();

        Map<String, String> properties = new HashMap<>();
        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        assertThat(result.get(RESPONDENT_NAME)).isEqualTo("Alice Smith and Beta Corp");
        assertThat(result.get(ALLOCATED_TRACK)).isEqualTo("Fast Track");
    }
}
