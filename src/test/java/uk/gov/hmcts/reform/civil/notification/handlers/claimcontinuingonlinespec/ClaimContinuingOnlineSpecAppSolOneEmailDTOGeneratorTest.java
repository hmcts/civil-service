package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecAppSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_SINGLE     = "template-single";
    private static final String TEMPLATE_MULTI      = "template-multi";
    private static final String ORG_NAME            = "Legal Org";
    private static final String REFERENCE_TEMPLATE  = "claim-continuing-online-notification-%s";
    public static final String ISSUED_ON = "issuedOn";
    public static final String CLAIM_DETAILS_NOTIFICATION_DEADLINE = "claimDetailsNotificationDeadline";
    public static final String PARTY_REFERENCES = "partyReferences";
    public static final String DEFENDANT_NAME = "defendantName";
    public static final String RESPONSE_DEADLINE = "responseDeadline";
    public static final String DEFENDANT_ONE_NAME = "defendantOneName";
    public static final String DEFENDANT_TWO_NAME = "defendantTwoName";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnSingleTemplate_whenNoSecondDefendant() {
        when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec())
                .thenReturn(TEMPLATE_SINGLE);

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();

        String id = emailGenerator.getEmailTemplateId(caseData);
        assertThat(id).isEqualTo(TEMPLATE_SINGLE);
    }

    @Test
    void shouldReturnMultiTemplate_whenSecondDefendantPresent() {
        when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2ForSpec())
                .thenReturn(TEMPLATE_MULTI);

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

        String id = emailGenerator.getEmailTemplateId(caseData);
        assertThat(id).isEqualTo(TEMPLATE_MULTI);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(emailGenerator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Nested
    class AddCustomPropertiesTests {

        @BeforeEach
        void stubOrgService() {
            when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));
        }

        @Test
        void singleDefendant_shouldIncludeDefendantNameAndResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .build();

            Map<String, String> props =
                    emailGenerator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(props)
                    .containsEntry(ISSUED_ON,
                            formatLocalDate(caseData.getIssueDate(), DATE))
                    .containsEntry(CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                            formatLocalDate(
                                    caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE))
                    .containsEntry(PARTY_REFERENCES,
                            buildPartiesReferencesEmailSubject(caseData))
                    .containsEntry(DEFENDANT_NAME,
                            getPartyNameBasedOnType(caseData.getRespondent1()))
                    .containsEntry(RESPONSE_DEADLINE,
                            formatLocalDateTime(
                                    caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));
        }

        @Test
        void twoDefendants_shouldIncludeBothDefendantNames() {
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

            Map<String, String> props =
                    emailGenerator.addCustomProperties(new HashMap<>(), caseData);

            assertThat(props)
                    .containsEntry(DEFENDANT_ONE_NAME,
                            getPartyNameBasedOnType(caseData.getRespondent1()))
                    .containsEntry(DEFENDANT_TWO_NAME,
                            getPartyNameBasedOnType(caseData.getRespondent2()));
        }
    }
}
