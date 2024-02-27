package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesForTab;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HelpWithFeesForTabService {

    public void setUpHelpWithFeeTab(CaseData.CaseDataBuilder caseDataBuilder) {
        CaseData caseData = caseDataBuilder.build();
        if (FeeType.CLAIMISSUED == caseData.getHwfFeeType()) {
            caseDataBuilder.claimIssuedHwfForTab(setUpClaimIssuedHelpWithFeeTab(caseData));
        } else if (FeeType.HEARING == caseData.getHwfFeeType()) {
            caseDataBuilder.hearingHwfForTab(setUpHearingHelpWithFeeTab(caseData));
        }
        return;
    }

    private HelpWithFeesForTab setUpHearingHelpWithFeeTab(CaseData caseData) {
        return HelpWithFeesForTab.builder()
            .remissionAmount(caseData.getHearingHwfDetails().getRemissionAmount())
            .applicantMustPay(caseData.getHearingHwfDetails().getOutstandingFeeInPounds())
            .claimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedHearingFeeInPence()))
            .feeCode(caseData.getHearingFee().getCode())
            .hwfType("Hearing Fee")
            .hwfReferenceNumber(caseData.getHelpWithFeesReferenceNumber())
            .build();
    }

    private HelpWithFeesForTab setUpClaimIssuedHelpWithFeeTab(CaseData caseData) {
        return HelpWithFeesForTab.builder()
            .remissionAmount(Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                                 .map(HelpWithFeesDetails::getRemissionAmount).isPresent()
                                 ? MonetaryConversions.penniesToPounds(caseData.getClaimIssuedHwfDetails().getRemissionAmount())
                                 : null)
            .applicantMustPay(Optional.ofNullable(caseData.getClaimIssuedHwfDetails())
                                  .map(HelpWithFeesDetails::getOutstandingFeeInPounds)
                                  .orElse(null))
            .claimFee(MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence()))
            .feeCode(caseData.getClaimFee().getCode())
            .hwfType("Claim Fee")
            .hwfReferenceNumber(caseData.getHelpWithFeesReferenceNumber())
            .build();
    }
}
