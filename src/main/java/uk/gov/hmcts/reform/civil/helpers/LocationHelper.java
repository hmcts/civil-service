package uk.gov.hmcts.reform.civil.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.UNSPEC_CLAIM;

@Slf4j
public class LocationHelper {

    private final BigDecimal ccmccAmount;
    private final String ccmccRegionId;
    private final String ccmccEpimsId;

    public LocationHelper(
        @Value("${genApp.lrd.ccmcc.amountPounds}") BigDecimal ccmccAmount,
        @Value("${genApp.lrd.ccmcc.epimsId}") String ccmccRegionId,
        @Value("${genApp.lrd.ccmcc.regionId}") String ccmccEpimsId) {
        this.ccmccAmount = ccmccAmount;
        this.ccmccRegionId = ccmccRegionId;
        this.ccmccEpimsId = ccmccEpimsId;
    }


    /**
     * If the defendant is individual or sole trader, their preferred court is the case's court.
     * Otherwise, the case's court is the claimant's preferred court.
     * In multiparty cases we consider the lead claimant and the lead defendant. Lead claimant is claimant 1,
     * lead defendant is the defendant who responded first to the claim.
     *
     * @param caseData case data
     * @return requested court to be used as case court
     */
    public Optional<RequestedCourt> getCaseManagementLocation(CaseData caseData) {
        List<RequestedCourt> prioritized = new ArrayList<>();
        boolean leadDefendantIs1 = leadDefendantIs1(caseData);
        Supplier<Party.Type> getDefendantType;
        Supplier<Optional<RequestedCourt>> getDefendantCourt;
        if (leadDefendantIs1) {
            log.debug("Case {}, lead defendant is 1", caseData.getLegacyCaseReference());
            getDefendantType = caseData.getRespondent1()::getType;
            getDefendantCourt = () -> Optional.ofNullable(caseData.getRespondent1DQ())
                .map(Respondent1DQ::getRespondent1DQRequestedCourt);
        } else {
            log.debug("Case {}, lead defendant is 2", caseData.getLegacyCaseReference());
            getDefendantType = caseData.getRespondent2()::getType;
            getDefendantCourt = () -> Optional.ofNullable(caseData.getRespondent2DQ())
                .map(Respondent2DQ::getRespondent2DQRequestedCourt);
        }

        if (EnumSet.of(Party.Type.INDIVIDUAL, Party.Type.SOLE_TRADER).contains(getDefendantType.get())) {
            log.debug(
                "Case {}, defendant is a person, so their court request has priority",
                caseData.getLegacyCaseReference()
            );
            getDefendantCourt.get()
                .filter(requestedCourt -> requestedCourt.getRequestHearingAtSpecificCourt() == YesOrNo.YES)
                .ifPresent(requestedCourt -> {
                    log.debug("Case {}, Defendant has requested a court", caseData.getLegacyCaseReference());
                    prioritized.add(requestedCourt);
                });
            getClaimantRequestedCourt(caseData).ifPresent(requestedCourt -> {
                log.debug("Case {}, Claimant has requested a court", caseData.getLegacyCaseReference());
                prioritized.add(requestedCourt);
            });
        } else {
            log.debug(
                "Case {}, defendant is a group, so claimant's court request has priority",
                caseData.getLegacyCaseReference()
            );
            getClaimantRequestedCourt(caseData)
                .filter(requestedCourt -> requestedCourt.getRequestHearingAtSpecificCourt() == YesOrNo.YES)
                .ifPresent(requestedCourt -> {
                    log.debug("Case {}, Claimant has requested a court", caseData.getLegacyCaseReference());
                    prioritized.add(requestedCourt);
                });
            getDefendantCourt.get().ifPresent(requestedCourt -> {
                log.debug("Case {}, Defendant has requested a court", caseData.getLegacyCaseReference());
                prioritized.add(requestedCourt);
            });
        }

        if ((caseData.getSuperClaimType() == SPEC_CLAIM
            && ccmccAmount.compareTo(caseData.getTotalClaimAmount()) > 0)
            || (caseData.getSuperClaimType() == UNSPEC_CLAIM
            && ccmccAmount.compareTo(caseData.getClaimValue().toPounds()) > 0)){
            // TODO use ccmcc as CaseLocation
        }
        return prioritized.stream().findFirst();
    }

    /**
     * Lead defendant is the defendant that first responded to claim.
     *
     * @param caseData case data
     * @return true if defendant 1 is lead defendant
     */
    private boolean leadDefendantIs1(CaseData caseData) {
        return caseData.getRespondent2ResponseDate() == null
            || (caseData.getRespondent1ResponseDate() != null
            && !caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate()));
    }

    /**
     * The lead claimant is always considered to be claimant1.
     *
     * @param caseData case data
     * @return requested court object for the lead claimant
     */
    private Optional<RequestedCourt> getClaimantRequestedCourt(CaseData caseData) {
        if (caseData.getSuperClaimType() == SPEC_CLAIM) {
            return Optional.ofNullable(caseData.getApplicant1DQ())
                .map(Applicant1DQ::getApplicant1DQRequestedCourt);
        } else {
            return Optional.ofNullable(caseData.getCourtLocation())
                .map(courtLocation -> RequestedCourt.builder()
                    .requestHearingAtSpecificCourt(YesOrNo.YES)
                    .responseCourtCode(courtLocation.getApplicantPreferredCourt())
                    .caseLocation(courtLocation.getCaseLocation())
                    .build());
        }
    }

    /**
     * We say that a locationRefData matches a RequestedCourt if the court code is the same or if
     * (a) the court's case location has region equal to locationRefData.regionId and (b) base location
     * equal to locationRefData.epimmsId.
     *
     * @param locations      list of locations
     * @param preferredCourt a preferred court
     * @return first matching location
     */
    private Optional<LocationRefData> getMatching(List<LocationRefData> locations, RequestedCourt preferredCourt) {
        if (preferredCourt == null) {
            return Optional.empty();
        }
        return locations.stream().filter(locationRefData -> matches(locationRefData, preferredCourt))
            .findFirst();
    }

    private boolean matches(LocationRefData locationRefData, RequestedCourt preferredCourt) {
        return (StringUtils.isNotBlank(preferredCourt.getResponseCourtCode())
            && preferredCourt.getResponseCourtCode().equals(locationRefData.getCourtLocationCode()))
            ||
            (preferredCourt.getCaseLocation() != null
                && StringUtils.equals(preferredCourt.getCaseLocation().getRegion(), locationRefData.getRegionId())
                && StringUtils.equals(
                preferredCourt.getCaseLocation().getBaseLocation(),
                locationRefData.getEpimmsId()
            ));
    }

    /**
     * The caseManagementLocation is given by the requestedCourt's caseLocation field. If there is a matching location,
     * we can also populate the locationName field.
     *
     * @param updatedData    data to update
     * @param requestedCourt the requested court to be used for the case
     * @param getLocations   how to get the list of locations
     * @return matching location
     */
    public Optional<LocationRefData> updateCaseManagementLocation(CaseData.CaseDataBuilder<?, ?> updatedData,
                                                                  RequestedCourt requestedCourt,
                                                                  Supplier<List<LocationRefData>> getLocations) {
        Optional<LocationRefData> matchingLocation = getMatching(getLocations.get(), requestedCourt);
        if (log.isDebugEnabled()) {
            String reference = updatedData.build().getLegacyCaseReference();
            log.debug("Case {}, requested court is {}", reference, requestedCourt != null ? "defined" : "undefined");
            log.debug(
                "Case {}, there {} a location matching to requested court",
                reference,
                matchingLocation.isPresent() ? "is" : "is not"
            );
        }
        updatedData
            .caseManagementLocation(Stream.of(
                    requestedCourt.getCaseLocation(),
                    matchingLocation.map(location ->
                                             CaseLocation.builder()
                                                 .region(location.getRegionId())
                                                 .baseLocation(location.getEpimmsId())
                                                 .build()
                    ).orElse(null)
                )
                                        .filter(this::isValidCaseLocation)
                                        .findFirst().orElseGet(CaseLocation::new));
        matchingLocation.map(LocationRefData::getSiteName).ifPresent(updatedData::locationName);
        return matchingLocation;
    }

    private boolean isValidCaseLocation(CaseLocation caseLocation) {
        return caseLocation != null && StringUtils.isNotBlank(caseLocation.getBaseLocation())
            && StringUtils.isNotBlank(caseLocation.getRegion());
    }
}
