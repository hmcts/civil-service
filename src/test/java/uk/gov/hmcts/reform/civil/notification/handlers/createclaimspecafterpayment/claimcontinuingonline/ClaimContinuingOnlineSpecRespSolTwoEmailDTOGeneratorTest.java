package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

public class ClaimContinuingOnlineSpecRespSolTwoEmailDTOGeneratorTest {

    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";
    private static final String TEMPLATE_ID = "resp-sol-one-template";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnTemplateId() {
        when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec())
                .thenReturn(TEMPLATE_ID);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
                .isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldAddCustomProperties() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("org name").build()));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent2ResponseDeadline(LocalDateTime.now())
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                .organisationID("org2")
                                .build())
                        .build())
                .build();

        Map<String, String> initial = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(initial, caseData);

        assertThat(result).containsEntry(
                CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getRespondent2ResponseDeadline().toLocalDate(), DATE)
        );
    }
}
