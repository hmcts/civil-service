package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

public class RequestedCourtSelectorTest {

    private RequestedCourtSelector requestedCourtSelector = new RequestedCourtSelector();

    @Test
    public void whenUnspecChooseClaimant() {
        String courtCode = "123";

        CaseData caseData = CaseData.builder()
            .superClaimType(SuperClaimType.UNSPEC_CLAIM)
            .courtLocation(CourtLocation.builder()
                               .applicantPreferredCourt(courtCode)
                               .build())
            .build();

        Assertions.assertEquals(
            courtCode,
            requestedCourtSelector.getPreferredRequestedCourt(caseData)
                .map(RequestedCourt::getResponseCourtCode).orElse(null)
        );
    }

    @Test
    public void whenSpecAndClaimantChooseClaimant() {
        String claimant = "123";
        String defendant = "234";

        CaseData caseData = CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(RequestedCourt.builder()
                                                              .responseCourtCode(claimant)
                                                              .build())
                              .build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                .responseCourtCode(defendant)
                                                                .build())
                               .build())
            .build();

        Assertions.assertEquals(
            claimant,
            requestedCourtSelector.getPreferredRequestedCourt(caseData)
                .map(RequestedCourt::getResponseCourtCode).orElse(null)
        );
    }

    @Test
    public void whenSpecAndNoClaimantChooseDefendant() {
        String claimant = "123";
        String defendant = "234";

        CaseData caseData = CaseData.builder()
            .superClaimType(SuperClaimType.SPEC_CLAIM)
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                .responseCourtCode(defendant)
                                                                .build())
                               .build())
            .build();

        Assertions.assertEquals(
            defendant,
            requestedCourtSelector.getPreferredRequestedCourt(caseData)
                .map(RequestedCourt::getResponseCourtCode).orElse(null)
        );
    }
}
