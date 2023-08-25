package uk.gov.hmcts.reform.civil.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@Component
public class LocationHelper {

    private static final Set<Party.Type> PEOPLE = EnumSet.of(Party.Type.INDIVIDUAL, Party.Type.SOLE_TRADER);
    private final BigDecimal ccmccAmount;
    private final String ccmccRegionId;
    private final String ccmccEpimsId;

    public LocationHelper(
        @Value("${genApp.lrd.ccmcc.amountPounds}") BigDecimal ccmccAmount,
        @Value("${genApp.lrd.ccmcc.epimsId}") String ccmccEpimsId,
        @Value("${genApp.lrd.ccmcc.regionId}") String ccmccRegionId) {

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

        if (CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && EnumSet.of(Party.Type.INDIVIDUAL, Party.Type.SOLE_TRADER).contains(getDefendantType.get())) {
            log.debug(
                "Case {}, defendant is a person, so their court request has priority",
                caseData.getLegacyCaseReference()
            );
            getDefendantCourt.get()
                .filter(this::hasInfo)
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
                .filter(this::hasInfo)
                .ifPresent(requestedCourt -> {
                    log.debug("Case {}, Claimant has requested a court", caseData.getLegacyCaseReference());
                    prioritized.add(requestedCourt);
                });
            getDefendantCourt.get().ifPresent(requestedCourt -> {
                log.debug("Case {}, Defendant has requested a court", caseData.getLegacyCaseReference());
                prioritized.add(requestedCourt);
            });
        }

        Optional<RequestedCourt> byParties = prioritized.stream().findFirst();
        if (ccmccAmount.compareTo(getClaimValue(caseData)) >= 0) {
            return Optional.of(byParties.map(requestedCourt -> requestedCourt.toBuilder()
                    .caseLocation(getCcmccCaseLocation()).build())
                                   .orElseGet(() -> RequestedCourt.builder()
                                       .caseLocation(getCcmccCaseLocation())
                                       .build()));
        } else {
            return byParties;
        }
    }

    private boolean hasInfo(RequestedCourt requestedCourt) {
        return StringUtils.isNotBlank(requestedCourt.getResponseCourtCode())
            || Optional.ofNullable(requestedCourt.getResponseCourtLocations())
            .map(DynamicList::getValue).isPresent();
    }

    private BigDecimal getClaimValue(CaseData caseData) {
        // super claim type is not always loaded
        return Stream.of(
                caseData.getTotalClaimAmount(),
                Optional.ofNullable(caseData.getClaimValue()).map(ClaimValue::toPounds).orElse(null)
            )
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(BigDecimal.ZERO);
    }

    private CaseLocationCivil getCcmccCaseLocation() {
        return CaseLocationCivil.builder().baseLocation(ccmccEpimsId).region(ccmccRegionId).build();
    }

    /**
     * Lead defendant is the defendant that first responded to claim.
     *
     * @param caseData case data
     * @return true if defendant 1 is lead defendant
     */
    public boolean leadDefendantIs1(CaseData caseData) {
        if (caseData.getRespondent2ResponseDate() == null) {
            return true;
        }
        boolean isPeople1 = PEOPLE.contains(caseData.getRespondent1().getType());
        boolean isPeople2 = PEOPLE.contains(caseData.getRespondent2().getType());
        if (isPeople1 == isPeople2) {
            return caseData.getRespondent1ResponseDate() != null
                && !caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate());
        } else {
            return isPeople1;
        }
    }

    /**
     * The lead claimant is always considered to be claimant1.
     *
     * @param caseData case data
     * @return requested court object for the lead claimant
     */
    public Optional<RequestedCourt> getClaimantRequestedCourt(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return getSpecClaimantRequestedCourt(caseData);
        } else {
            return getUnspecClaimantRequestedCourt(caseData);
        }
    }

    private Optional<RequestedCourt> getSpecClaimantRequestedCourt(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicant1DQ())
            .map(Applicant1DQ::getApplicant1DQRequestedCourt);
    }

    private Optional<RequestedCourt> getUnspecClaimantRequestedCourt(CaseData caseData) {
        return Optional.ofNullable(caseData.getCourtLocation())
            .map(courtLocation -> RequestedCourt.builder()
                .requestHearingAtSpecificCourt(YesOrNo.YES)
                .responseCourtCode(courtLocation.getApplicantPreferredCourt())
                .caseLocation(courtLocation.getCaseLocation())
                .build());
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

    public Optional<LocationRefData> getMatching(List<LocationRefData> locations, RequestedCourt preferredCourt) {
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
     * Centralized creation of CaseLocationCivil from LocationRefData to reduce the places it can be done.
     *
     * @param location mandatory
     * @return case location built from location
     */
    public static CaseLocationCivil buildCaseLocation(LocationRefData location) {
        return CaseLocationCivil.builder()
            .region(location.getRegionId())
            .baseLocation(location.getEpimmsId())
            .build();
    }

    /**
     * Updates both caseManagementLocation and locationName with the same LocationRefData to ease not forgetting
     * about one of those.
     *
     * @param builder  (mandatory) to build a case data
     * @param location (mandatory) what to update with
     */
    public static void updateWithLocation(CaseData.CaseDataBuilder<?, ?> builder, LocationRefData location) {
        builder
            .caseManagementLocation(buildCaseLocation(location))
            .locationName(location.getSiteName());
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
                    Optional.ofNullable(requestedCourt).map(RequestedCourt::getCaseLocation),
                    matchingLocation.map(LocationHelper::buildCaseLocation)
                ).filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .filter(this::isValidCaseLocation)
                                        .findFirst().orElseGet(CaseLocationCivil::new));
        matchingLocation.map(LocationRefData::getSiteName).ifPresent(updatedData::locationName);
        return matchingLocation;
    }

    private boolean isValidCaseLocation(CaseLocationCivil caseLocation) {
        return caseLocation != null && StringUtils.isNotBlank(caseLocation.getBaseLocation())
            && StringUtils.isNotBlank(caseLocation.getRegion());
    }
}
