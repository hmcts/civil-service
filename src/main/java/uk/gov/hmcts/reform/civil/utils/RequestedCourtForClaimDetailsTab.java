package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RequestedCourtForTabDetails;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearing;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;

import java.util.Optional;

public final class RequestedCourtForClaimDetailsTab {

    private RequestedCourtForClaimDetailsTab() {
        // no op
    }

    private static RequestedCourtForTabDetails createCourtDetails(DQ courtDetails) {
        return RequestedCourtForTabDetails.builder()
            .requestedCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getResponseCourtCode))
                    .orElse(null)
            )
            .reasonForHearingAtSpecificCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getReasonForHearingAtSpecificCourt))
                    .orElse(null)
            )
            .requestHearingHeldRemotely(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearing())
                        .map(RemoteHearing::getRemoteHearingRequested))
                    .orElse(null)
            )
            .requestHearingHeldRemotelyReason(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearing())
                        .map(RemoteHearing::getReasonForRemoteHearing))
                    .orElse(null)
            )
            .build();
    }

    private static RequestedCourtForTabDetails createCourtDetailsSpec(DQ courtDetails) {
        return RequestedCourtForTabDetails.builder()
            .requestedCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getResponseCourtCode))
                    .orElse(null)
            )
            .reasonForHearingAtSpecificCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getReasonForHearingAtSpecificCourt))
                    .orElse(null)
            )
            .requestHearingHeldRemotely(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearingLRspec())
                        .map(RemoteHearingLRspec::getRemoteHearingRequested))
                    .orElse(null)
            )
            .requestHearingHeldRemotelyReason(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearingLRspec())
                        .map(RemoteHearingLRspec::getReasonForRemoteHearing))
                    .orElse(null)
            )
            .build();
    }

    public static void updateRequestCourtClaimTabApplicant(CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ appRequestedCourt = updatedData.build().getApplicant1DQ();
        updatedData.requestedCourtForTabDetailsApp(createCourtDetails(appRequestedCourt));
    }

    public static void updateRequestCourtClaimTabApplicantSpec(CaseData.CaseDataBuilder<?, ?> updatedData) {
        //Applicant spec response remote hearing, stores data in different field
        DQ appRequestedCourt = updatedData.build().getApplicant1DQ();
        updatedData.requestedCourtForTabDetailsApp(createCourtDetailsSpec(appRequestedCourt));
    }

    public static void updateRequestCourtClaimTabRespondent1Spec(CaseData.CaseDataBuilder<?, ?> updatedData) {
        //Respondent1 spec response remote hearing, stores data in different field
        DQ res1RequestedCourt = updatedData.build().getRespondent1DQ();
        updatedData.requestedCourtForTabDetailsRes1(createCourtDetailsSpec(res1RequestedCourt));
    }

    public static void updateRequestCourtClaimTabRespondent2Spec(CaseData.CaseDataBuilder<?, ?> updatedData) {
        //Respondent2 spec response remote hearing, stores data in different field
        DQ res2RequestedCourt = updatedData.build().getRespondent2DQ();
        updatedData.requestedCourtForTabDetailsRes2(createCourtDetailsSpec(res2RequestedCourt));
    }

    public static void updateRequestCourtClaimTabRespondent1(CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ res1RequestedCourt = updatedData.build().getRespondent1DQ();
        updatedData.requestedCourtForTabDetailsRes1(createCourtDetails(res1RequestedCourt));
    }

    public static void updateRequestCourtClaimTabRespondent2(CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ res2RequestedCourt = updatedData.build().getRespondent2DQ();
        updatedData.requestedCourtForTabDetailsRes2(createCourtDetails(res2RequestedCourt));
    }
}
