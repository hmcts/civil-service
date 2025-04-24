package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RequestedCourtForTabDetails;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearing;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestedCourtForClaimDetailsTab {

    private final LocationReferenceDataService locationRefDataService;

    public RequestedCourtForTabDetails createCourtDetails(String auth, DQ courtDetails) {
        return RequestedCourtForTabDetails.builder()
            .requestedCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getResponseCourtCode))
                    .orElse(null)
            )
            .requestedCourtName(
                Optional.ofNullable(courtDetails)
                    .flatMap(details -> Optional.ofNullable(getCourtName(auth, details)))
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

    public RequestedCourtForTabDetails createCourtDetailsSpec(String auth, DQ courtDetails) {
        return RequestedCourtForTabDetails.builder()
            .requestedCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getResponseCourtCode))
                    .orElse(null)
            )
            .requestedCourtName(
                Optional.ofNullable(courtDetails)
                    .flatMap(details -> Optional.ofNullable(getCourtName(auth, details)))
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

    private String getCourtName(String auth, DQ courtDetail) {
        LocationRefData courtLocationDetails;
        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(auth);
        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(courtDetail.getRequestedCourt().getCaseLocation().getBaseLocation())).toList();
        if (!foundLocations.isEmpty()) {
            courtLocationDetails = foundLocations.get(0);
        } else {
            throw new IllegalArgumentException("Requested Court Location not found, in location data");
        }
        return courtLocationDetails.getSiteName();
    }

    public void updateRequestCourtClaimTabApplicant(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ appRequestedCourt = updatedData.build().getApplicant1DQ();
        updatedData.requestedCourtForTabDetailsApp(createCourtDetails(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                      appRequestedCourt));
    }

    public void updateRequestCourtClaimTabApplicantSpec(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ appRequestedCourt = updatedData.build().getApplicant1DQ();
        updatedData.requestedCourtForTabDetailsApp(createCourtDetailsSpec(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                          appRequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent1Spec(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ res1RequestedCourt = updatedData.build().getRespondent1DQ();
        updatedData.requestedCourtForTabDetailsRes1(createCourtDetailsSpec(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                           res1RequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent2Spec(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ res2RequestedCourt = updatedData.build().getRespondent2DQ();
        updatedData.requestedCourtForTabDetailsRes2(createCourtDetailsSpec(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                           res2RequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent1(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ res1RequestedCourt = updatedData.build().getRespondent1DQ();
        updatedData.requestedCourtForTabDetailsRes1(createCourtDetails(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                       res1RequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent2(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData) {
        DQ res2RequestedCourt = updatedData.build().getRespondent2DQ();
        updatedData.requestedCourtForTabDetailsRes2(createCourtDetails(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                       res2RequestedCourt));
    }
}
