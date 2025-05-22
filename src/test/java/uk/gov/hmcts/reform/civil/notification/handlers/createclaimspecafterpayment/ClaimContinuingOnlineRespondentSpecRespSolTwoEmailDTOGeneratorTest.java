package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.TemplateCommonPropertiesHelper;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineRespondentSpecRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "resp-sol-two-template";
    private static final String ORG_NAME = "Second Respondent Org";
    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private TemplateCommonPropertiesHelper templateCommonPropertiesHelper;

    @InjectMocks
    private ClaimContinuingOnlineRespondentSpecRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnTemplateId() {
        when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec())
                .thenReturn(TEMPLATE_ID);

        CaseData data = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();

        String id = generator.getEmailTemplateId(data);
        assertThat(id).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String tpl = generator.getReferenceTemplate();
        assertThat(tpl).isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldNotifyOnlyWhenSecondRespondentExists() {
        CaseData yes = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
        CaseData no = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();

        assertThat(generator.getShouldNotify(yes)).isTrue();
        assertThat(generator.getShouldNotify(no)).isFalse();
    }

    @Test
    void shouldAddDeadlineToProperties() {
        CaseData data = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();

        Map<String, String> props = generator.addProperties(data);
        assertThat(props)
                .containsEntry(
                        CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                        formatLocalDate(data.getRespondent1ResponseDeadline().toLocalDate(), DATE));
    }

    @Test
    void shouldAddRespondent2OrgNameWhenDifferentRepresentative() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

        CaseData data = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .build();

        Map<String, String> props = generator.addCustomProperties(new HashMap<>(), data);
        assertThat(props).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
    }

    @Test
    void shouldAddRespondent1OrgNameWhenSameRepresentative() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

        CaseData data = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .build();

        Map<String, String> props = generator.addCustomProperties(new HashMap<>(), data);
        assertThat(props).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
    }
}