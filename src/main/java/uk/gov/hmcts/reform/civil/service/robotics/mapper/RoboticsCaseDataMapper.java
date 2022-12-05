package uk.gov.hmcts.reform.civil.service.robotics.mapper;


import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.CaseHeader;
import uk.gov.hmcts.reform.civil.model.robotics.ClaimDetails;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;

import java.math.BigDecimal;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;

@Service
public abstract class RoboticsCaseDataMapper {

    public abstract RoboticsCaseDataSpec toRoboticsCaseData(CaseData caseData);

    protected ClaimDetails buildClaimDetails(CaseData caseData) {
        BigDecimal claimInterest = caseData.getTotalInterest() != null
            ? caseData.getTotalInterest() : BigDecimal.ZERO;
        BigDecimal amountClaimedWithInterest = caseData.getTotalClaimAmount().add(claimInterest);
        return ClaimDetails.builder()
            .amountClaimed(amountClaimedWithInterest)
            .courtFee(ofNullable(caseData.getClaimFee())
                          .map(fee -> penniesToPounds(fee.getCalculatedAmountInPence()))
                          .orElse(null))
            .caseIssuedDate(ofNullable(caseData.getIssueDate())
                                .map(issueDate -> issueDate.format(ISO_DATE))
                                .orElse(null))
            .caseRequestReceivedDate(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
            .build();
    }

    protected CaseHeader buildCaseHeader(CaseData caseData) {
        return CaseHeader.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .owningCourtCode("700")
            .owningCourtName("Online Civil Money Claim")
            .caseType("CLAIM - SPEC ONLY")
            .preferredCourtCode("")
            .caseAllocatedTo("")
            .build();
    }
}
