package uk.gov.hmcts.reform.civil.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HWFFeePaymentOutcomeService {

    public void updateHwfReferenceNumber(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {

        if (Objects.nonNull(caseData.getFeePaymentOutcomeDetails())) {
            if (caseData.isHWFTypeClaimIssued()) {
                HelpWithFees helpWithFees = HelpWithFees.builder()
                    .helpWithFee(YesOrNo.YES)
                    .helpWithFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome())
                    .build();
                caseData.getCaseDataLiP().setHelpWithFees(helpWithFees);
            }
            if (caseData.isHWFTypeHearing()) {
                builder.hearingHelpFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome());
            }
            caseData = builder.build();
            clearHwfReferenceProperties(caseData);
        }
    }

    private void clearHwfReferenceProperties(CaseData caseData) {
        caseData.getFeePaymentOutcomeDetails().setHwfNumberAvailable(null);
        caseData.getFeePaymentOutcomeDetails().setHwfNumberForFeePaymentOutcome(null);
    }
}
