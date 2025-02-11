package uk.gov.hmcts.reform.civil.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

@Slf4j
@Component
public class LocationHelper {

    private static final Set<Party.Type> PEOPLE = EnumSet.of(Party.Type.INDIVIDUAL, Party.Type.SOLE_TRADER);
    private final BigDecimal ccmccAmount;
    private final String ccmccRegionId;
    private final String ccmccEpimsId;
    private final String cnbcRegionId;
    private final String cnbcEpimsId;
    private final FeatureToggleService featureToggleService;

    public LocationHelper(
        @Value("${genApp.lrd.ccmcc.amountPounds}") BigDecimal ccmccAmount,
        @Value("${genApp.lrd.ccmcc.epimsId}") String ccmccEpimsId,
        @Value("${genApp.lrd.ccmcc.regionId}") String ccmccRegionId,
        @Value("${court-location.specified-claim.epimms-id}") String cnbcEpimsId,
        @Value("${court-location.specified-claim.region-id}") String cnbcRegionId,
        FeatureToggleService featureToggleService) {

        this.ccmccAmount = ccmccAmount;
        this.ccmccRegionId = ccmccRegionId;
        this.ccmccEpimsId = ccmccEpimsId;
        this.cnbcEpimsId = cnbcEpimsId;
        this.cnbcRegionId = cnbcRegionId;
        this.featureToggleService = featureToggleService;
    }

    public Optional<RequestedCourt> getCaseManagementLocation(CaseData caseData) {
        return getCaseManagementLocationDefault(caseData, false);
    }

    public Optional<RequestedCourt> getCaseManagementLocationWhenLegalAdvisorSdo(CaseData caseData, boolean legalAdvisorSdo) {
        return getCaseManagementLocationDefault(caseData, legalAdvisorSdo);
    }

    private Optional<RequestedCourt> getCaseManagementLocationDefault(CaseData caseData, boolean isLegalAdvisorSdo) {
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

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && ccmccAmount.compareTo(getClaimValue(caseData)) >= 0) {
            if (!isLegalAdvisorSdo) {
                log.debug("Case {}, specified claim under 1000, CML set to CCMCC", caseData.getLegacyCaseReference());
                return Optional.of(RequestedCourt.builder().caseLocation(getCcmccCaseLocation()).build());
            } else {
                log.debug("Case {}, specified claim under 1000, Legal advisor,  CML set to preferred location", caseData.getLegacyCaseReference());
                assignSpecPreferredCourt(caseData, getDefendantType, getDefendantCourt, prioritized);
                return prioritized.stream().findFirst();
            }
        }

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory()) && ccmccAmount.compareTo(getClaimValue(caseData)) <= 0) {
            if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
                && isMultiOrIntTrack(caseData)
                && caseData.isLipCase()) {
                return Optional.of(RequestedCourt.builder().caseLocation(getCnbcCaseLocation()).build());
            } else {
                assignSpecPreferredCourt(caseData, getDefendantType, getDefendantCourt, prioritized);
                return prioritized.stream().findFirst();
            }
        }

        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
                && isMultiOrIntTrack(caseData)
                && caseData.isLipCase()) {
                return Optional.of(RequestedCourt.builder().caseLocation(getCnbcCaseLocation()).build());
            } else {
                getClaimantRequestedCourt(caseData).ifPresent(requestedCourt -> {
                    log.debug("Case {}, Claimant has requested a court", caseData.getLegacyCaseReference());
                    prioritized.add(requestedCourt);
                });
                return prioritized.stream().findFirst();
            }
        }
        return Optional.empty();
    }

    private void assignSpecPreferredCourt(CaseData caseData, Supplier<Party.Type> getDefendantType,
                                      Supplier<Optional<RequestedCourt>> getDefendantCourt, List<RequestedCourt> prioritized) {
        if (PEOPLE.contains(getDefendantType.get())) {
            getDefendantCourt.get()
                .filter(this::hasInfo)
                .ifPresent(requestedCourt -> {
                    log.debug("Case {}, Defendant has requested a court", caseData.getLegacyCaseReference());
                    prioritized.add(requestedCourt);
                });
        }
        getClaimantRequestedCourt(caseData).ifPresent(requestedCourt -> {
            log.debug("Case {}, Claimant has requested a court", caseData.getLegacyCaseReference());
            prioritized.add(requestedCourt);
        });
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

    private CaseLocationCivil getCnbcCaseLocation() {
        return CaseLocationCivil.builder().baseLocation(cnbcEpimsId).region(cnbcRegionId).build();
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
        Long reference = updatedData.build().getCcdCaseReference();
        if (log.isInfoEnabled()) {
            log.info("Case {}, requested court is {}", reference, requestedCourt != null ? "defined" : "undefined");
            log.info(
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

    private boolean isMultiOrIntTrack(CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack())
                || AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack());
        } else {
            return AllocatedTrack.INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())
                || AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack());
        }
    }
}
