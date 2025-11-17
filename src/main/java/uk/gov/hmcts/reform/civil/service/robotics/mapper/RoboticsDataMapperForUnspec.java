package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.robotics.CaseHeader;
import uk.gov.hmcts.reform.civil.model.robotics.ClaimDetails;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsCaseDataSupport;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;

@Slf4j
@Service
public class RoboticsDataMapperForUnspec extends BaseRoboticsDataMapper {

    private final EventHistoryMapper eventHistoryMapper;
    private final OrganisationService organisationService;
    private final LocationRefDataUtil locationRefDataUtil;
    private final RoboticsCaseDataSupport caseDataSupport;

    public RoboticsDataMapperForUnspec(RoboticsAddressMapper addressMapper,
                                       EventHistoryMapper eventHistoryMapper,
                                       OrganisationService organisationService,
                                       LocationRefDataUtil locationRefDataUtil,
                                       RoboticsCaseDataSupport caseDataSupport) {
        super(addressMapper);
        this.eventHistoryMapper = eventHistoryMapper;
        this.organisationService = organisationService;
        this.locationRefDataUtil = locationRefDataUtil;
        this.caseDataSupport = caseDataSupport;
    }

    public RoboticsCaseData toRoboticsCaseData(CaseData caseData, String authToken) {
        log.info("Preparing Robotics data for unspec caseId {}", caseData.getCcdCaseReference());
        requireNonNull(caseData);
        log.info("Starting RoboticsCaseData mapping for caseId={}", caseData.getCcdCaseReference());

        var header = buildCaseHeader(caseData, authToken);
        log.info("RoboticsCaseData CaseHeader built: {}", header);

        var parties = buildLitigiousParties(caseData);
        log.info("RoboticsCaseData LitigiousParties built: {}", parties);

        var claimDetails = buildClaimDetails(caseData);
        log.info("RoboticsCaseData ClaimDetails built: {}", claimDetails);

        var events = eventHistoryMapper.buildEvents(caseData, authToken);
        log.info("RoboticsCaseData Events built: {}", events);

        var roboticsBuilder = RoboticsCaseData.builder()
            .header(header)
            .litigiousParties(parties)
            .claimDetails(claimDetails)
            .events(events);

        if (!(caseData.isLipvLipOneVOne())) {
            roboticsBuilder.solicitors(buildSolicitors(caseData));
        }

        if (caseData.getCcdState() == PROCEEDS_IN_HERITAGE_SYSTEM
            || caseData.getCcdState() == CASE_DISMISSED) {
            roboticsBuilder.noticeOfChange(RoboticsDataUtil.buildNoticeOfChange(caseData));
        }

        return roboticsBuilder.build();
    }

    private ClaimDetails buildClaimDetails(CaseData caseData) {
        ClaimDetails.ClaimDetailsBuilder claimDetailsBuilder = ClaimDetails.builder();

        if (!caseData.isLipvLipOneVOne()) {
            claimDetailsBuilder.amountClaimed(caseData.getClaimValue().toPounds());
        }

        return claimDetailsBuilder
            .courtFee(ofNullable(caseData.getClaimFee())
                        .map(fee -> penniesToPounds(fee.getCalculatedAmountInPence()))
                        .orElse(null))
            .caseIssuedDate(ofNullable(caseData.getIssueDate())
                        .map(issueDate -> issueDate.format(ISO_DATE))
                        .orElse(null))
            .caseRequestReceivedDate(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
            .build();
    }

    private CaseHeader buildCaseHeader(CaseData caseData, String authToken) {
        return CaseHeader.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .owningCourtCode("807")
            .owningCourtName("CCMCC")
            .caseType(getCaseType(caseData))
            .preferredCourtCode(locationRefDataUtil.getPreferredCourtData(caseData, authToken, true))
            .caseAllocatedTo(buildAllocatedTrack(caseData.getAllocatedTrack(), caseData.getResponseClaimTrack()))
            .build();
    }

    private String getCaseType(CaseData caseData) {
        if (ClaimTypeUnspec.PERSONAL_INJURY.equals(caseData.getClaimTypeUnSpec())) {
            return "PERSONAL INJURY";
        }
        return "CLAIM - UNSPEC ONLY";
    }

    private String buildAllocatedTrack(AllocatedTrack allocatedTrack, String responseClaimTrack) {
        if (allocatedTrack == null) {
            if (responseClaimTrack == null) {
                return "";
            }
            return switch (responseClaimTrack) {
                case "FAST_CLAIM" -> "FAST TRACK";
                case "SMALL_CLAIM" -> "SMALL CLAIM TRACK";
                default -> "";
            };
        }
        return switch (allocatedTrack) {
            case FAST_CLAIM -> "FAST TRACK";
            case MULTI_CLAIM -> "MULTI TRACK";
            case SMALL_CLAIM -> "SMALL CLAIM TRACK";
            case INTERMEDIATE_CLAIM -> "INTERMEDIATE TRACK";
            default -> "";
        };
    }

    private List<Solicitor> buildSolicitors(CaseData caseData) {
        List<Solicitor> solicitorsList = new ArrayList<>();
        solicitorsList.add(buildApplicantSolicitor(caseData, APPLICANT_SOLICITOR_ID));
        ofNullable(buildRespondentSolicitor(caseData, RESPONDENT_SOLICITOR_ID))
            .ifPresent(solicitorsList::add);

        if (YES == caseData.getRespondent2Represented() && YES != caseData.getRespondent2SameLegalRepresentative()) {
            ofNullable(buildRespondent2Solicitor(caseData, RESPONDENT2_SOLICITOR_ID))
                .ifPresent(solicitorsList::add);
        }
        return solicitorsList;
    }

    private Solicitor buildRespondentSolicitor(CaseData caseData, String id) {
        String organisationId = OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData);
        var organisationDetails = ofNullable(caseData.getRespondentSolicitor1OrganisationDetails());
        if (organisationId == null && organisationDetails.isEmpty()) {
            return null;
        }

        return caseDataSupport.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id(id)
                .isPayee(false)
                .organisationId(organisationId)
                .contactEmailAddress(caseData.getRespondentSolicitor1EmailAddress())
                .reference(ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getRespondentSolicitor1Reference)
                    .map(s -> s.substring(0, Math.min(s.length(), 24)))
                    .orElse(null))
                .serviceAddress(caseData.getRespondentSolicitor1ServiceAddress())
                .organisation(fetchOrganisation(organisationId, caseData))
                .organisationDetails(organisationDetails.orElse(null))
                .build()
        );
    }

    private Solicitor buildApplicantSolicitor(CaseData caseData, String id) {
        Optional<String> organisationId = getOrganisationId(caseData.getApplicant1OrganisationPolicy());

        return caseDataSupport.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id(id)
                .isPayee(true)
                .organisationId(organisationId.orElse(null))
                .contactEmailAddress(ofNullable(caseData.getApplicantSolicitor1UserDetails())
                    .map(user -> user.getEmail())
                    .orElse(null))
                .reference(ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .map(s -> s.substring(0, Math.min(s.length(), 24)))
                    .orElse(null))
                .serviceAddress(caseData.getApplicantSolicitor1ServiceAddress())
                .organisation(fetchOrganisation(organisationId.orElse(null), caseData))
                .organisationDetails(null)
                .build()
        );
    }

    private List<LitigiousParty> buildLitigiousParties(CaseData caseData) {
        String respondent1SolicitorId = caseData.getRespondent1Represented() == YES
            ? RESPONDENT_SOLICITOR_ID : null;
        LocalDateTime notificationDateTime = caseData.getClaimDetailsNotificationDate();
        LocalDateTime defaultDateTime = notificationDateTime;
        var respondentParties = new ArrayList<>(List.of(
            caseDataSupport.buildLitigiousParty(
                caseData.getApplicant1(),
                caseData.getApplicant1LitigationFriend(),
                "Claimant",
                APPLICANT_ID,
                APPLICANT_SOLICITOR_ID,
                caseData.isLipvLipOneVOne()
                    ? null
                    : caseDataSupport.organisationId(caseData.getApplicant1OrganisationPolicy()).orElse(null),
                ofNullable(defaultDateTime).map(LocalDateTime::toLocalDate).orElse(null)
            ),
            caseDataSupport.buildLitigiousParty(
                caseData.getRespondent1(),
                caseData.getRespondent1LitigationFriend(),
                "Defendant",
                RESPONDENT_ID,
                respondent1SolicitorId,
                OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData),
                ofNullable(defaultDateTime).map(LocalDateTime::toLocalDate).orElse(null)
            )
        ));

        if (caseData.getApplicant2() != null) {
            respondentParties.add(caseDataSupport.buildLitigiousParty(
                caseData.getApplicant2(),
                caseData.getApplicant2LitigationFriend(),
                "Claimant",
                APPLICANT2_ID,
                APPLICANT_SOLICITOR_ID,
                caseDataSupport.organisationId(caseData.getApplicant2OrganisationPolicy()).orElse(null),
                ofNullable(defaultDateTime).map(LocalDateTime::toLocalDate).orElse(null)
            ));
        }

        if (caseData.getRespondent2() != null) {
            String respondent2SolicitorId = null;
            if (caseData.getRespondent2Represented() == YES
                && caseData.getRespondent2SameLegalRepresentative() == YES) {
                respondent2SolicitorId = RESPONDENT_SOLICITOR_ID;
            } else if (caseData.getRespondent2Represented() == YES
                && caseData.getRespondent2SameLegalRepresentative() == NO) {
                respondent2SolicitorId = RESPONDENT2_SOLICITOR_ID;
            }
            respondentParties.add(caseDataSupport.buildLitigiousParty(
                caseData.getRespondent2(),
                caseData.getRespondent2LitigationFriend(),
                "Defendant",
                RESPONDENT2_ID,
                respondent2SolicitorId,
                OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData),
                ofNullable(defaultDateTime).map(LocalDateTime::toLocalDate).orElse(null)
            ));
        }
        return respondentParties;
    }

    private Solicitor buildRespondent2Solicitor(CaseData caseData, String id) {
        String organisationId = OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData);

        var organisationDetails = ofNullable(
            caseData.getRespondentSolicitor2OrganisationDetails()
        );
        if (organisationId == null && organisationDetails.isEmpty()) {
            return null;
        }
        return caseDataSupport.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id(id)
                .isPayee(false)
                .organisationId(organisationId)
                .contactEmailAddress(caseData.getRespondentSolicitor2EmailAddress())
                .reference(ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getRespondentSolicitor2Reference)
                    .map(s -> s.substring(0, Math.min(s.length(), 24)))
                    .orElse(null))
                .serviceAddress(caseData.getRespondentSolicitor2ServiceAddress())
                .organisation(fetchOrganisation(organisationId, caseData))
                .organisationDetails(organisationDetails.orElse(null))
                .build()
        );
    }

    private uk.gov.hmcts.reform.civil.prd.model.Organisation fetchOrganisation(String organisationId, CaseData caseData) {
        if (organisationId == null) {
            return null;
        }
        try {
            return organisationService.findOrganisationById(organisationId).orElse(null);
        } catch (FeignException e) {
            log.error("Error recovering org id {} for case id {}", organisationId, caseData.getLegacyCaseReference(), e);
            return null;
        }
    }
}
