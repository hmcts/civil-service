package uk.gov.hmcts.reform.civil.service.judgments;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgementAddress;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDefendantDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.JudgmentDetailsCJES;
import uk.gov.hmcts.reform.civil.model.judgmentonline.cjes.RegistrationType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Component
@AllArgsConstructor
public class CjesMapper {

    private static final String SPEC_SERVICE_ID = "AAA6";
    private static final String UNSPEC_SERVICE_ID = "AAA7";

    public JudgmentDetailsCJES toJudgmentDetailsCJES(JudgmentDetails judgmentDetails, CaseData caseData) {
        JudgmentDetailsCJES.JudgmentDetailsCJESBuilder requestBody = JudgmentDetailsCJES.builder();

        if (judgmentDetails != null) {
            requestBody
                .serviceId(SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) ? SPEC_SERVICE_ID : UNSPEC_SERVICE_ID)
                .judgmentId(judgmentDetails.getJudgmentId().toString())
                .judgmentEventTimeStamp(judgmentDetails.getLastUpdateTimeStamp())
                .courtEPIMsId(judgmentDetails.getCourtLocation())
                .ccdCaseRef(caseData.getCcdCaseReference().toString())
                .caseNumber(caseData.getLegacyCaseReference())
                .judgmentAdminOrderTotal(Double.valueOf(judgmentDetails.getTotalAmount()))
                .judgmentAdminOrderDate(judgmentDetails.getIssueDate())
                .registrationType(RegistrationType.valueOf(judgmentDetails.getRtlState()).getRegistrationType())
                .cancellationDate(judgmentDetails.getCancelDate())
                .defendant1(JudgmentDefendantDetails.builder()
                                .defendantName(judgmentDetails.getDefendant1Name())
                                .defendantDateOfBirth(judgmentDetails.getDefendant1Dob())
                                .defendantAddress(judgementAddressMapper(judgmentDetails.getDefendant1Address()))
                                .build());

            if (judgmentDetails.getDefendant2Name() != null && judgmentDetails.getDefendant2Address() != null) {
                requestBody.defendant2(JudgmentDefendantDetails.builder()
                                           .defendantName(judgmentDetails.getDefendant2Name())
                                           .defendantDateOfBirth(judgmentDetails.getDefendant2Dob())
                                           .defendantAddress(judgementAddressMapper(judgmentDetails.getDefendant2Address()))
                                           .build());
            }

            return requestBody.build();
        }
        return null;
    }

    private JudgementAddress judgementAddressMapper(Address address) {
        return JudgementAddress.builder()
            .defendantAddressLine1(address.getAddressLine1())
            .defendantAddressLine2(address.getAddressLine2())
            .defendantAddressLine3(address.getAddressLine3())
            .defendantAddressLine4(address.getPostTown())
            .defendantAddressLine5(address.getCountry())
            .defendantPostCode(address.getPostCode())
            .build();
    }
}
