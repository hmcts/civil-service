package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.robotics.CaseHeader;
import uk.gov.hmcts.reform.civil.model.robotics.ClaimDetails;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsCaseDataSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsCaseDataSupport.SolicitorData;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoboticsDataMapper {

    private final RoboticsCaseDataSupport caseDataSupport;
    private final EventHistoryMapper eventHistoryMapper;
    private final OrganisationService organisationService;
    private final LocationRefDataUtil locationRefDataUtil;
    private final RoboticsPartyLookup partyLookup;

    public RoboticsCaseData toRoboticsCaseData(CaseData caseData, String authToken) {
        log.info("Preparing Robotics data for unspec caseId {}", caseData.getCcdCaseReference());
        requireNonNull(caseData);
        var roboticsBuilder = RoboticsCaseData.builder()
            .header(buildCaseHeader(caseData, authToken))
            .litigiousParties(buildLitigiousParties(caseData))
            .claimDetails(buildClaimDetails(caseData))
            .events(eventHistoryMapper.buildEvents(caseData, authToken));

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
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = null;
        if (organisationId != null) {
            try {
                organisation = organisationService.findOrganisationById(organisationId).orElse(null);
            } catch (FeignException e) {
                log.error("Error recovering org id " + organisationId
                              + " for case id " + caseData.getLegacyCaseReference(), e);
            }
        }
        return caseDataSupport.buildSolicitor(
            SolicitorData.builder()
                .id(id)
                .isPayee(false)
                .organisationId(organisationId)
                .contactEmailAddress(caseData.getRespondentSolicitor1EmailAddress())
                .reference(ofNullable(caseData.getSolicitorReferences())
                               .map(SolicitorReferences::getRespondentSolicitor1Reference)
                               .orElse(null))
                .serviceAddress(caseData.getRespondentSolicitor1ServiceAddress())
                .organisation(organisation)
                .organisationDetails(organisationDetails.orElse(null))
                .build()
        );
    }

    private Solicitor buildApplicantSolicitor(CaseData caseData, String id) {
        Optional<String> organisationId = caseDataSupport.organisationId(caseData.getApplicant1OrganisationPolicy());

        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = null;
        try {
            organisation = organisationId
                .flatMap(organisationService::findOrganisationById)
                .orElse(null);
        } catch (FeignException e) {
            log.error("Error recovering org id " + organisationId.orElse(null)
                + " for case id " + caseData.getLegacyCaseReference(), e);
        }

        return caseDataSupport.buildSolicitor(
            SolicitorData.builder()
                .id(id)
                .isPayee(true)
                .organisationId(organisationId.orElse(null))
                .contactEmailAddress(caseData.getApplicantSolicitor1UserDetails().getEmail())
                .reference(ofNullable(caseData.getSolicitorReferences())
                               .map(SolicitorReferences::getApplicantSolicitor1Reference)
                               .orElse(null))
                .serviceAddress(caseData.getApplicantSolicitor1ServiceAddress())
                .organisation(organisation)
                .build()
        );
    }

    private List<LitigiousParty> buildLitigiousParties(CaseData caseData) {
        String respondent1SolicitorId = caseData.getRespondent1Represented() == YES
            ? RESPONDENT_SOLICITOR_ID : null;

        LocalDate notificationDate = ofNullable(caseData.getClaimDetailsNotificationDate())
            .map(LocalDateTime::toLocalDate)
            .orElse(null);

        String applicant1OrgId = caseData.isLipvLipOneVOne()
            ? null
            : caseDataSupport.organisationId(caseData.getApplicant1OrganisationPolicy()).orElse(null);

        var respondentParties = new ArrayList<>(List.of(
            caseDataSupport.buildLitigiousParty(
                caseData.getApplicant1(),
                caseData.getApplicant1LitigationFriend(),
                "Claimant",
                partyLookup.applicantId(0),
                APPLICANT_SOLICITOR_ID,
                applicant1OrgId,
                notificationDate
            ),
            caseDataSupport.buildLitigiousParty(
                caseData.getRespondent1(),
                caseData.getRespondent1LitigationFriend(),
                "Defendant",
                partyLookup.respondentId(0),
                respondent1SolicitorId,
                OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData),
                notificationDate
            )
        ));

        if (caseData.getApplicant2() != null) {
            String applicant2OrgId = caseDataSupport.organisationId(caseData.getApplicant2OrganisationPolicy())
                .orElse(null);
            respondentParties.add(caseDataSupport.buildLitigiousParty(
                caseData.getApplicant2(),
                caseData.getApplicant2LitigationFriend(),
                "Claimant",
                partyLookup.applicantId(1),
                APPLICANT_SOLICITOR_ID,
                applicant2OrgId,
                notificationDate
            ));
        }

        if (caseData.getRespondent2() != null) {
            String respondent2SolicitorId = caseDataSupport.resolveRespondentSolicitorId(
                caseData.getRespondent2Represented(),
                caseData.getRespondent2SameLegalRepresentative()
            );
            respondentParties.add(caseDataSupport.buildLitigiousParty(
                caseData.getRespondent2(),
                caseData.getRespondent2LitigationFriend(),
                "Defendant",
                partyLookup.respondentId(1),
                respondent2SolicitorId,
                OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData),
                notificationDate
            ));
        }
        return respondentParties;
    }

    private Solicitor buildRespondent2Solicitor(CaseData caseData, String id) {
        String organisationId = OrgPolicyUtils.getRespondent2SolicitorOrgId(caseData);

        var organisationDetails = ofNullable(caseData.getRespondentSolicitor2OrganisationDetails());
        if (organisationId == null && organisationDetails.isEmpty()) {
            return null;
        }
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation = null;
        if (organisationId != null) {
            organisation = organisationService.findOrganisationById(organisationId).orElse(null);
        }

        return caseDataSupport.buildSolicitor(
            SolicitorData.builder()
                .id(id)
                .isPayee(false)
                .organisationId(organisationId)
                .contactEmailAddress(caseData.getRespondentSolicitor2EmailAddress())
                .reference(ofNullable(caseData.getSolicitorReferences())
                               .map(SolicitorReferences::getRespondentSolicitor2Reference)
                               .orElse(null))
                .serviceAddress(caseData.getRespondentSolicitor2ServiceAddress())
                .organisation(organisation)
                .organisationDetails(organisationDetails.orElse(null))
                .build()
        );
    }
}
