package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.TemplateCommonPropertiesHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class ClaimantLipHelpWithFeesEmailClaimantDTOGeneratorTest {

    private static final String EMAIL = "claimant@hmcts.net";
    private static final String ENGLISH_TEMPLATE = "english-template";
    private static final String WELSH_TEMPLATE = "welsh-template";
    private static final String NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES_NOTIFICATION = "notify-claimant-lip-help-with-fees-notification-%s";
    private static final String LEGACY_REFERENCE = "1594901956117591";
    public static final String TASK_ID = "reference";
    public static final String CLAIMANT_EMAIL = "claimant@example.com";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private TemplateCommonPropertiesHelper templateCommonPropertiesHelper;

    @InjectMocks
    private ClaimantLipHelpWithFeesClaimantEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build()
                .toBuilder()
                .legacyCaseReference(LEGACY_REFERENCE)
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

        ReflectionTestUtils.setField(
                generator,
                "templateCommonPropertiesHelper",
                templateCommonPropertiesHelper
        );
    }

    @Test
    void shouldReturnEnglishTemplateAndCorrectParams() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFees())
                .thenReturn(ENGLISH_TEMPLATE);

        EmailDTO dto = generator.buildEmailDTO(caseData, TASK_ID);

        Map<String, String> params = dto.getParameters();
        assertThat(dto.getEmailTemplate()).isEqualTo(ENGLISH_TEMPLATE);
        assertThat(params)
                .containsKey(CLAIMANT_V_DEFENDANT);
    }

    @Test
    void shouldReturnWelshTemplateWhenBilingual() {
        when(notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh())
                .thenReturn(WELSH_TEMPLATE);

        caseData = caseData.toBuilder()
                .claimantBilingualLanguagePreference(Language.BOTH.toString())
                .build();

        EmailDTO dto = generator.buildEmailDTO(caseData, TASK_ID);
        assertThat(dto.getEmailTemplate()).isEqualTo(WELSH_TEMPLATE);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
                .isEqualTo(NOTIFY_CLAIMANT_LIP_HELP_WITH_FEES_NOTIFICATION);
    }

    @Test
    void shouldReturnTrueWhenClaimantEmailIsNotNull() {
         caseData = CaseData.builder()
                .claimantUserDetails(IdamUserDetails.builder().email(CLAIMANT_EMAIL).build())
                .build();

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenClaimantEmailIsNull() {
         caseData = CaseData.builder()
                .claimantUserDetails(IdamUserDetails.builder().email(null).build())
                .build();

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }
}