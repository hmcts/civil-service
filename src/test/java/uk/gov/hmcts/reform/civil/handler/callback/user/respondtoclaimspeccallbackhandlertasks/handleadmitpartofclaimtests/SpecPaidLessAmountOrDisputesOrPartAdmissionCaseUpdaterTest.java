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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldSetSpecPaidLessAmountOrDisputesOrPartAdmissionToYes_whenConditionsAreMet() {
        UnavailableDate unavailableDate = new UnavailableDate();
        unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
        unavailableDate.setDate(LocalDate.now().plusDays(4));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT);
        caseData.setDefenceRouteRequired(DISPUTES_THE_CLAIM);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        SmallClaimHearing smallClaimHearing = new SmallClaimHearing();
        smallClaimHearing.setUnavailableDatesRequired(YES);
        List<Element<UnavailableDate>> dates = Stream.of(unavailableDate
        ).map(ElementUtils::element).toList();
        smallClaimHearing.setSmallClaimUnavailableDate(dates);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQHearingSmallClaim(smallClaimHearing);
        caseData.setRespondent1DQ(respondent1DQ);

        updater.update(caseData);

        assertThat(caseData.getSpecPaidLessAmountOrDisputesOrPartAdmission()).isEqualTo(YES);
    }
}
