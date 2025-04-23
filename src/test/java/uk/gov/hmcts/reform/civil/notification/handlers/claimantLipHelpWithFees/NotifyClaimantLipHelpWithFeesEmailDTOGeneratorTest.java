package uk.gov.hmcts.reform.civil.notification.handlers.claimantLipHelpWithFees;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class NotifyClaimantLipHelpWithFeesEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyClaimantLipHelpWithFeesEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build()
                .toBuilder()
                .claimantUserDetails(
                        IdamUserDetails.builder().email("claimant@hmcts.net").build()
                )
                .applicant1(
                        Party.builder()
                                .individualFirstName("John")
                                .individualLastName("Doe")
                                .type(Party.Type.INDIVIDUAL)
                                .build()
                )
                .respondent1(
                        Party.builder()
                                .individualFirstName("Jane")
                                .individualLastName("Roe")
                                .type(Party.Type.INDIVIDUAL)
                                .build()
                )
                .build();
    }

    @Test
    void shouldReturnEnglishTemplateAndCorrectParams() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFees())
                .thenReturn("english-template");

        EmailDTO dto = generator.buildEmailDTO(caseData);

        Map<String, String> params = dto.getParameters();
        assertThat(params)
                .containsEntry("claimReferenceNumber", "000DC001")
                .containsEntry("claimantName", "John Doe")
                .containsKey(CLAIMANT_V_DEFENDANT);
    }

    @Test
    void shouldReturnWelshTemplateWhenBilingual() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh())
                .thenReturn("welsh-template");

        caseData = caseData.toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();

        EmailDTO dto = generator.buildEmailDTO(caseData);
        assertThat(dto.getEmailTemplate()).isEqualTo("welsh-template");
    }

    @Test
    void shouldExposeCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
                .isEqualTo("notify-claimant-lip-help-with-fees-notification-%s");
    }
}
