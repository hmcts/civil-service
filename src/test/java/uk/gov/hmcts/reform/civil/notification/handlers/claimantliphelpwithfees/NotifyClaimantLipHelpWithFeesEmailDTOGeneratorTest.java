package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

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

    public static final String EMAIL = "claimant@hmcts.net";
    public static final String ENGLISH_TEMPLATE = "english-template";
    public static final String WELSH_TEMPLATE = "welsh-template";
    public static final String NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES_NOTIFICATION = "notify-claimant-lip-help-with-fees-notification-%s";
    public static final String LEGACY_REFERENCE = "000DC001";
    public static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    public static final String CLAIMANT_NAME = "claimantName";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private ClaimantLipHelpWithFeesEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build()
                .toBuilder()
                .claimantUserDetails(
                        IdamUserDetails.builder().email(EMAIL).build()
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
    void shouldAlwaysReturnTrueForGetShouldNotify() {
        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldReturnEnglishTemplateAndCorrectParams() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFees())
                .thenReturn(ENGLISH_TEMPLATE);

        EmailDTO dto = generator.buildEmailDTO(caseData);

        Map<String, String> params = dto.getParameters();
        assertThat(params)
                .containsEntry(CLAIM_REFERENCE_NUMBER, LEGACY_REFERENCE)
                .containsEntry(CLAIMANT_NAME, "John Doe")
                .containsKey(CLAIMANT_V_DEFENDANT);
    }

    @Test
    void shouldReturnWelshTemplateWhenBilingual() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh())
                .thenReturn(WELSH_TEMPLATE);

        caseData = caseData.toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();

        EmailDTO dto = generator.buildEmailDTO(caseData);
        assertThat(dto.getEmailTemplate()).isEqualTo(WELSH_TEMPLATE);
    }

    @Test
    void shouldExposeCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
                .isEqualTo(NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES_NOTIFICATION);
    }
}
