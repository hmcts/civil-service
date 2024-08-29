package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.fulldefence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FullDefenceRespondentSolicitorTwoCCSpecNotifierTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private FullDefenceRespondentSolicitorTwoCCSpecNotifier notifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        when(notificationsProperties.getClaimantSolicitorDefendantResponseFullDefence()).thenReturn("template-id");
    }

    @Test
    void shouldNotifyRespondentSolicitor2In1v2ScenarioSecondSol_whenV1CallbackInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .respondent2(PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build())
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();

        when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec())
            .thenReturn("template-id");

        notifier.notifySolicitorForDefendantResponse(caseData);

        verify(notificationService).sendMail(
            "respondentsolicitor2@example.com",
            "template-id",
            notifier.addProperties(caseData),
            "defendant-response-applicant-notification-000DC001"
        );
    }
}
