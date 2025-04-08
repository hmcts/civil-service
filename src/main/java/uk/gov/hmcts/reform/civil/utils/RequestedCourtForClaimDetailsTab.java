package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RequestedCourtForTabDetails;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

public final class RequestedCourtForClaimDetailsTab {

    private static final String POPULATE_REQUESTED_COURT_LABEL = "The %s legal representative requested the following court location code: %s";

    private RequestedCourtForClaimDetailsTab() {
        // Private constructor to prevent instantiation
    }

    private static RequestedCourtForTabDetails createCourtDetails(RequestedCourt courtDetails, String party) {
        return RequestedCourtForTabDetails.builder()
            .requestHearingAtSpecificCourt(courtDetails.getRequestHearingAtSpecificCourt())
            .reasonForHearingAtSpecificCourt(courtDetails.getReasonForHearingAtSpecificCourt())
            .requestedCourt(format(POPULATE_REQUESTED_COURT_LABEL, party, courtDetails.getResponseCourtCode()))
            .build();
    }

    public static void updateRequestCourtClaimTabApplicant(CaseData.CaseDataBuilder<?, ?> updatedData) {
        var caseData = updatedData.build();
        var requestedCourt = caseData.getApplicant1DQ().getApplicant1DQRequestedCourt();

        if (nonNull(requestedCourt)) {
            updatedData.requestedCourtForTabDetailsRes1(createCourtDetails(requestedCourt, "applicant"));
        }
    }

    public static void updateRequestCourtClaimTabRespondent1(CaseData.CaseDataBuilder<?, ?> updatedData) {
        var caseData = updatedData.build();
        var requestedCourt = caseData.getRespondent1DQ().getRespondent1DQRequestedCourt();
        System.out.println("HEREEEE");
        if (nonNull(requestedCourt)) {
            System.out.println("HEREEEE inside");
            updatedData.requestedCourtForTabDetailsRes1(createCourtDetails(requestedCourt, "respondent"));
        }
    }

    public static void updateRequestCourtClaimTabRespondent2(CaseData.CaseDataBuilder<?, ?> updatedData) {
        var caseData = updatedData.build();
        var requestedCourt = caseData.getRespondent2DQ().getRespondent2DQRequestedCourt();

        if (nonNull(requestedCourt)) {
            updatedData.requestedCourtForTabDetailsRes1(createCourtDetails(requestedCourt, "respondent"));
        }
    }
}
