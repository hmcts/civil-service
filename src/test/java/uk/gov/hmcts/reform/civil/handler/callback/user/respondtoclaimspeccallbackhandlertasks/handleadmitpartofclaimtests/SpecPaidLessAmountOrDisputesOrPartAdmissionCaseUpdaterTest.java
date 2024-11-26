package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.SpecPaidLessAmountOrDisputesOrPartAdmissionCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SpecPaidLessAmountOrDisputesOrPartAdmissionCaseUpdaterTest {

    @InjectMocks
    private SpecPaidLessAmountOrDisputesOrPartAdmissionCaseUpdater updater;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldSetSpecPaidLessAmountOrDisputesOrPartAdmissionToYes_whenConditionsAreMet() {
        List<Element<UnavailableDate>> dates = Stream.of(
                UnavailableDate.builder()
                        .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                        .date(LocalDate.now().plusDays(4))
                        .build()
        ).map(ElementUtils::element).toList();

        CaseData caseData = CaseData.builder()
                .respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT)
                .defenceRouteRequired(DISPUTES_THE_CLAIM)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent1DQ(Respondent1DQ.builder()
                        .respondent1DQHearingSmallClaim(SmallClaimHearing.builder()
                                .unavailableDatesRequired(YES)
                                .smallClaimUnavailableDate(dates)
                                .build())
                        .build())
                .build();

        updater.update(caseData, caseDataBuilder);

        assertThat(caseDataBuilder.build().getSpecPaidLessAmountOrDisputesOrPartAdmission()).isEqualTo(YES);
    }
}