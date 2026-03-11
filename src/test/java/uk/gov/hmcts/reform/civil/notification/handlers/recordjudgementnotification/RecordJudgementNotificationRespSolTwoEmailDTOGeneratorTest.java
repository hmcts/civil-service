package uk.gov.hmcts.reform.civil.notification.handlers.recordjudgementnotification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;

public class RecordJudgementNotificationRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private RecordJudgementNotificationRespSolTwoEmailDTOGenerator generator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnLrTemplateId() {
        CaseData caseData = baseCaseData();
        when(notificationsProperties.getNotifyLrRecordJudgmentDeterminationMeansTemplate()).thenReturn("template-id");

        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo("template-id");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("record-judgment-determination-means-notification-%s");
    }

    @Test
    void shouldAddDefendantNameProperty() {
        CaseData caseData = baseCaseData();
        Map<String, String> result = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(result.get(DEFENDANT_NAME)).isEqualTo(getDefendantNameBasedOnCaseType(caseData));
    }

    private CaseData baseCaseData() {
        return new CaseDataBuilder()
            .atStateClaimDraft()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2OrganisationPolicy(sampleOrganisationPolicy())
            .build();
    }

    private OrganisationPolicy sampleOrganisationPolicy() {
        return new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("org-id"));
    }
}
