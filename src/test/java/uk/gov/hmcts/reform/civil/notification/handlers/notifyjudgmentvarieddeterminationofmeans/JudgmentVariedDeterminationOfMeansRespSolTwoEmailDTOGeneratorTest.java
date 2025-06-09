package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_SPEC;

@ExtendWith(MockitoExtension.class)
class JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "template-id";
    public static final String DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS = "defendant-judgment-varied-determination-of-means-%s";
    private static final long CLAIM_REF  = 12345L;
    private static final String TEST_ORG_NAME = "Test Org Name";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private JudgmentVariedDeterminationOfMeansRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnTemplateIdAndReferenceTemplate() {
        when(notificationsProperties.getNotifyDefendantJudgmentVariedDeterminationOfMeansTemplate())
                .thenReturn(TEMPLATE_ID);

        CaseData cd = CaseData.builder().build();
        assertThat(generator.getEmailTemplateId(cd)).isEqualTo(TEMPLATE_ID);
        assertThat(generator.getReferenceTemplate()).isEqualTo(DEFENDANT_JUDGMENT_VARIED_DETERMINATION_OF_MEANS);
    }

    @Test
    void shouldAddCustomPropertiesCorrectly() {
        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CLAIM_REF)
                .respondent2Represented(YesOrNo.YES)
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                .organisationID("F1")
                                .build())
                        .build())
                .build();

        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(TEST_ORG_NAME).build()));

        Map<String, String> properties = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(properties, caseData);

        Map<String, String> expectedProps = Map.of(
                DEFENDANT_NAME_SPEC, TEST_ORG_NAME
        );

        assertThat(result).containsAllEntriesOf(expectedProps);
    }
}
