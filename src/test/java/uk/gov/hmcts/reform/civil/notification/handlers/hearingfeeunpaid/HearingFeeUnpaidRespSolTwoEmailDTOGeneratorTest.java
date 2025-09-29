package uk.gov.hmcts.reform.civil.notification.handlers.hearingfeeunpaid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@ExtendWith(MockitoExtension.class)
class HearingFeeUnpaidRespSolTwoEmailDTOGeneratorTest {

    public static final String TEMPLATE_ID = "template-id";
    public static final String HEARING_FEE_UNPAID_RESPONDENT_NOTIFICATION = "hearing-fee-unpaid-respondent-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private HearingFeeUnpaidRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectTemplateId() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline().build();
        when(notificationsProperties.getRespondentHearingFeeUnpaid()).thenReturn(TEMPLATE_ID);

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo(HEARING_FEE_UNPAID_RESPONDENT_NOTIFICATION);
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastHearingFeeDueDeadline()
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID("OrgId").build())
                        .build())
                .build();

        when(organisationService.findOrganisationById("OrgId"))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().name("Test Org Name").build()));

        Map<String, String> properties = new HashMap<>();
        String formattedDate = formatLocalDate(caseData.getHearingDate(), DATE);

        Map<String, String> updatedProperties = generator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties).containsAllEntriesOf(Map.of(
                HEARING_DATE, formattedDate,
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, false, organisationService)
        ));
    }
}