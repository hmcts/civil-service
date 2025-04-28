package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

public class ClaimantResponseNotAgreedRepaymentDefendantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantResponseNotAgreedRepaymentDefendantEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder()
            .caseDataLiP(
                CaseDataLiP.builder()
                    .respondent1LiPResponse(
                        RespondentLiPResponse.builder()
                            .respondent1ResponseLanguage(BOTH.toString())
                            .build()
                    ).build()
            ).build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLipWelshTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";
        when(notificationsProperties.getNotifyDefendantLipTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("claimant-reject-repayment-respondent-notification-%s");
    }

    @Test
    void shouldAddCustomProperties() {
        CaseData caseData = CaseData.builder().build();
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);

        String partyName = "party name";
        partyUtilsMockedStatic.when(() -> getPartyNameBasedOnType(any())).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        assertThat(updatedProperties.size()).isEqualTo(1);
        assertThat(updatedProperties).containsEntry(DEFENDANT_NAME, partyName);
    }
}
