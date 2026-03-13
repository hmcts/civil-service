package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RequestedCourtForTabDetails;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
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
        return new RequestedCourtForTabDetails()
            .setRequestedCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getResponseCourtCode))
                    .orElse(null)
            )
            .setRequestedCourtName(
                Optional.ofNullable(courtDetails)
                    .flatMap(details -> Optional.ofNullable(getCourtName(auth, details)))
                    .orElse(null)
            )
            .setReasonForHearingAtSpecificCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getReasonForHearingAtSpecificCourt))
                    .orElse(null)
            )
            .setRequestHearingHeldRemotely(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearing())
                        .map(RemoteHearing::getRemoteHearingRequested))
                    .orElse(null)
            )
            .setRequestHearingHeldRemotelyReason(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearing())
                        .map(RemoteHearing::getReasonForRemoteHearing))
                    .orElse(null)
            );
    }

    public RequestedCourtForTabDetails createCourtDetailsSpec(String auth, DQ courtDetails) {
        return new RequestedCourtForTabDetails()
            .setRequestedCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getResponseCourtCode))
                    .orElse(null)
            )
            .setRequestedCourtName(
                Optional.ofNullable(courtDetails)
                    .flatMap(details -> Optional.ofNullable(getCourtName(auth, details)))
                    .orElse(null)
            )
            .setReasonForHearingAtSpecificCourt(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRequestedCourt())
                        .map(RequestedCourt::getReasonForHearingAtSpecificCourt))
                    .orElse(null)
            )
            .setRequestHearingHeldRemotely(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearingLRspec())
                        .map(RemoteHearingLRspec::getRemoteHearingRequested))
                    .orElse(null)
            )
            .setRequestHearingHeldRemotelyReason(
                Optional.ofNullable(courtDetails).flatMap(details -> Optional.ofNullable(details.getRemoteHearingLRspec())
                        .map(RemoteHearingLRspec::getReasonForRemoteHearing))
                    .orElse(null)
            );
    }

    private String getCourtName(String auth, DQ courtDetail) {
        LocationRefData courtLocationDetails;
        List<LocationRefData> locationRefDataList = locationRefDataService.getHearingCourtLocations(auth);

        String preferredBaseLocation = Optional.ofNullable(courtDetail)
            .map(DQ::getRequestedCourt)
            .map(RequestedCourt::getCaseLocation)
            .map(CaseLocationCivil::getBaseLocation)
            .orElse(null);
        if (preferredBaseLocation == null) {
            return null;
        }

        var foundLocations = locationRefDataList.stream()
            .filter(location -> location.getEpimmsId().equals(preferredBaseLocation)).toList();
        if (!foundLocations.isEmpty()) {
            courtLocationDetails = foundLocations.get(0);
        } else {
            log.info("Preferred requested Court Location not found in location data");
            return null;
        }
        return courtLocationDetails.getSiteName();
    }

    public void updateRequestCourtClaimTabApplicant(CallbackParams callbackParams, CaseData updatedData) {
        DQ appRequestedCourt = updatedData.getApplicant1DQ();
        updatedData.setRequestedCourtForTabDetailsApp(createCourtDetails(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                      appRequestedCourt));
    }

    public void updateRequestCourtClaimTabApplicantSpec(CallbackParams callbackParams, CaseData caseData) {
        DQ appRequestedCourt = caseData.getApplicant1DQ();
        caseData.setRequestedCourtForTabDetailsApp(createCourtDetailsSpec(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                          appRequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent1Spec(CaseData caseData, CallbackParams callbackParams) {
        DQ res1RequestedCourt = caseData.getRespondent1DQ();
        caseData.setRequestedCourtForTabDetailsRes1(createCourtDetailsSpec(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                           res1RequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent2Spec(CallbackParams callbackParams, CaseData updatedData) {
        DQ res2RequestedCourt = updatedData.getRespondent2DQ();
        updatedData.setRequestedCourtForTabDetailsRes2(createCourtDetailsSpec(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                           res2RequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent1(CallbackParams callbackParams, CaseData caseData) {
        DQ res1RequestedCourt = caseData.getRespondent1DQ();
        caseData.setRequestedCourtForTabDetailsRes1(createCourtDetails(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                       res1RequestedCourt));
    }

    public void updateRequestCourtClaimTabRespondent2(CallbackParams callbackParams, CaseData caseData) {
        DQ res2RequestedCourt = caseData.getRespondent2DQ();
        caseData.setRequestedCourtForTabDetailsRes2(createCourtDetails(callbackParams.getParams().get(BEARER_TOKEN).toString(),
                                                                       res2RequestedCourt));
    }
}
