package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimanthwfoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.openMocks;

class NotifyClaimantHwFOutcomeAppSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private NotifyClaimantHwFOutcomeAppSolOneEmailDTOGenerator generator;

//    private static final CaseData CLAIM_ISSUE_CASE_DATA = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
//        .applicant1(PartyBuilder.builder().individual().build().toBuilder()
//                        .partyEmail("test@email.com")
//                        .build())
//        .respondent1Represented(YesOrNo.NO)
//        .specRespondent1Represented(YesOrNo.NO)
//        .applicant1Represented(YesOrNo.NO)
//        .caseDataLiP(CaseDataLiP.builder().helpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber(
//            "000HWF001").build()).build())
//        .claimFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(100000)).build())
//        .hwfFeeType(FeeType.CLAIMISSUED)
//        .build();

    @BeforeEach
    void setUp() {
        openMocks(this);
        generator = new NotifyClaimantHwFOutcomeAppSolOneEmailDTOGenerator(notificationsProperties);
    }

    @Test
    void shouldReturnApplicantEmailWhenApplicantIsLiP() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().partyEmail("applicant@example.com").build())
            .applicant1Represented(YesOrNo.NO)
            .build();

        String emailAddress = generator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo("applicant@example.com");
    }

    @Test
    void shouldReturnSolicitorEmailWhenApplicantIsRepresented() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.YES)
            .applicantSolicitor1UserDetails(
                uk.gov.hmcts.reform.civil.model.IdamUserDetails.builder()
                    .email("solicitor@example.com")
                    .build()
            )
            .build();

        String emailAddress = generator.getEmailAddress(caseData);

        assertThat(emailAddress).isEqualTo("solicitor@example.com");
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("hwf-outcome-notification-%s");
    }
}
