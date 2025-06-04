package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

public class ClaimContinuingOnlineSpecRespSolOneEmailDTOGeneratorTest {

    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";
    private static final String TEMPLATE_ID = "resp-sol-one-template";
    private static final String ORG_NAME = "Respondent Org";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator generator;

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
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        Map<String, String> initial = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(initial, caseData);
        assertThat(result).containsAllEntriesOf(Map.of(
                CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, getRespondentLegalOrganizationName(
                        caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()),
                CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)
        ));
    }

    public String getRespondentLegalOrganizationName(String id) {
        Optional<Organisation> organisation = organisationService.findOrganisationById(id);
        String respondentLegalOrganizationName = null;
        if (organisation.isPresent()) {
            respondentLegalOrganizationName = organisation.get().getName();
        }
        return respondentLegalOrganizationName;
    }
}

