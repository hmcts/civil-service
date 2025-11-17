package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.robotics.CaseHeader;
import uk.gov.hmcts.reform.civil.model.robotics.ClaimDetails;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsCaseDataSupport;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_SOLICITOR_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_SOLICITOR_ID;

/**
 * This class is skeleton to be refined after we have final version of RPA Json structure
 * and it's mapping with CaseData.
 */
@Slf4j
@Service
public class RoboticsDataMapperForSpec extends BaseRoboticsDataMapper {

    private final EventHistoryMapper eventHistoryMapper;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final RoboticsCaseDataSupport caseDataSupport;

    public RoboticsDataMapperForSpec(RoboticsAddressMapper addressMapper,
                                     EventHistoryMapper eventHistoryMapper,
                                     OrganisationService organisationService,
                                     FeatureToggleService featureToggleService,
                                     RoboticsCaseDataSupport caseDataSupport) {
        super(addressMapper);
        this.eventHistoryMapper = eventHistoryMapper;
        this.organisationService = organisationService;
        this.featureToggleService = featureToggleService;
        this.caseDataSupport = caseDataSupport;
    }

    public RoboticsCaseDataSpec toRoboticsCaseData(CaseData caseData, String authToken) {
        log.info("Preparing Robotics data for spec caseId {}", caseData.getCcdCaseReference());
        requireNonNull(caseData);
        log.info("Starting RoboticsCaseData mapping for caseId={}", caseData.getCcdCaseReference());

        var header = buildCaseHeader(caseData);
        log.info("RoboticsCaseDataSpec CaseHeader built: {}", header);

        var parties = buildLitigiousParties(caseData);
        log.info("RoboticsCaseDataSpec LitigiousParties built: {}", parties);

        var solicitors = buildSolicitors(caseData);
        log.info("RoboticsCaseDataSpec Solicitors built: {}", solicitors);

        var claimDetails = buildClaimDetails(caseData);
        log.info("RoboticsCaseDataSpec ClaimDetails built: {}", claimDetails);

        var events = eventHistoryMapper.buildEvents(caseData, authToken);
        log.info("RoboticsCaseDataSpec Events built: {}", events);

        var builder = RoboticsCaseDataSpec.builder()
            .header(header)
            .litigiousParties(parties)
            .solicitors(solicitors)
            .claimDetails(claimDetails)
            .events(events);

        if (caseData.getCcdState() == PROCEEDS_IN_HERITAGE_SYSTEM
            || caseData.getCcdState() == CASE_DISMISSED) {
            builder.noticeOfChange(RoboticsDataUtil.buildNoticeOfChange(caseData));
        }

        return builder.build();
    }

    private ClaimDetails buildClaimDetails(CaseData caseData) {
        BigDecimal claimInterest = caseData.getTotalInterest() != null
            ? caseData.getTotalInterest() : BigDecimal.ZERO;
        BigDecimal amountClaimedWithInterest = caseData.getTotalClaimAmount().add(claimInterest);
        return ClaimDetails.builder()
            .amountClaimed(amountClaimedWithInterest)
            .courtFee(ClaimFeeUtility.getCourtFee(caseData))
            .caseIssuedDate(ofNullable(caseData.getIssueDate())
                .map(issueDate -> issueDate.format(ISO_DATE))
                .orElse(null))
            .caseRequestReceivedDate(caseData.getSubmittedDate().toLocalDate().format(ISO_DATE))
            .build();
    }

    private CaseHeader buildCaseHeader(CaseData caseData) {
        return CaseHeader.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .owningCourtCode("700")
            .owningCourtName("Online Civil Money Claim")
            .caseType("CLAIM - SPEC ONLY")
            .preferredCourtCode("")
            .caseAllocatedTo("")
            .build();
    }

    private List<Solicitor> buildSolicitors(CaseData caseData) {
        List<Solicitor> solicitorsList = new ArrayList<>();
        ofNullable(buildApplicantSolicitor(caseData))
            .ifPresent(solicitorsList::add);
        ofNullable(buildRespondentSolicitor(caseData))
            .ifPresent(solicitorsList::add);

        if (YES == caseData.getSpecRespondent2Represented()
            && YES != caseData.getRespondent2SameLegalRepresentative()) {
            ofNullable(buildRespondent2Solicitor(caseData))
                .ifPresent(solicitorsList::add);
        }
        return solicitorsList;
    }

    private Solicitor buildRespondentSolicitor(CaseData caseData) {
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder = Solicitor.builder();
        Optional<String> organisationId = getOrganisationId(caseData.getRespondent1OrganisationPolicy());
        var organisationDetails = ofNullable(
            caseData.getRespondentSolicitor1OrganisationDetails()
        );
        if (organisationId.isEmpty() && organisationDetails.isEmpty()) {
            return null;
        }
        var solicitorEmail = ofNullable(
            caseData.getRespondentSolicitor1EmailAddress()
        );
        return caseDataSupport.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id(RoboticsDataUtil.RESPONDENT_SOLICITOR_ID)
                .isPayee(false)
                .organisationId(organisationId.orElse(null))
                .contactEmailAddress(solicitorEmail.orElse(null))
                .reference(ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getRespondentSolicitor1Reference)
                    .map(s -> s.substring(0, Math.min(s.length(), 24)))
                    .orElse(null))
                .serviceAddress(caseData.getSpecRespondentCorrespondenceAddressdetails())
                .organisation(organisationId.flatMap(organisationService::findOrganisationById).orElse(null))
                .organisationDetails(organisationDetails.orElse(null))
                .build()
        );
    }

    private Solicitor buildRespondent2Solicitor(CaseData caseData) {
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder = Solicitor.builder();
        Optional<String> organisationId = getOrganisationId(caseData.getRespondent2OrganisationPolicy());

        var organisationDetails = ofNullable(
            caseData.getRespondentSolicitor2OrganisationDetails()
        );
        if (organisationId.isEmpty() && organisationDetails.isEmpty()) {
            return null;
        }
        return caseDataSupport.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id(RoboticsDataUtil.RESPONDENT2_SOLICITOR_ID)
                .isPayee(false)
                .organisationId(organisationId.orElse(null))
                .reference(ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getRespondentSolicitor2Reference)
                    .map(s -> s.substring(0, Math.min(s.length(), 24)))
                    .orElse(null))
                .serviceAddress(caseData.getSpecRespondent2CorrespondenceAddressdetails())
                .organisation(organisationId.flatMap(organisationService::findOrganisationById).orElse(null))
                .organisationDetails(organisationDetails.orElse(null))
                .contactEmailAddress(caseData.getRespondentSolicitor2EmailAddress())
                .build()
        );
    }

    private Solicitor buildApplicantSolicitor(CaseData caseData) {
        if (featureToggleService.isLipVLipEnabled() && (caseData.isLipvLipOneVOne() || NO.equals(caseData.getApplicant1Represented()))) {
            return null;
        }
        Optional<String> organisationId = getOrganisationId(caseData.getApplicant1OrganisationPolicy());
        var providedServiceAddress = caseData.getSpecApplicantCorrespondenceAddressdetails();
        return caseDataSupport.buildSolicitor(
            RoboticsCaseDataSupport.SolicitorData.builder()
                .id(RoboticsDataUtil.APPLICANT_SOLICITOR_ID)
                .isPayee(true)
                .organisationId(organisationId.orElse(null))
                .contactEmailAddress(ofNullable(caseData.getApplicantSolicitor1UserDetails())
                    .map(user -> user.getEmail())
                    .orElse(null))
                .reference(ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .map(s -> s.substring(0, Math.min(s.length(), 24)))
                    .orElse(null))
                .serviceAddress(providedServiceAddress)
                .organisation(organisationId.flatMap(organisationService::findOrganisationById).orElse(null))
                .organisationDetails(null)
                .build()
        );
    }

    private List<LitigiousParty> buildLitigiousParties(CaseData caseData) {
        LocalDate dateOfService = caseData.getIssueDate();
        String respondent1SolicitorId = caseData.getSpecRespondent1Represented() == YES
            ? RESPONDENT_SOLICITOR_ID : null;

        var respondentParties = new ArrayList<>(List.of(
            caseDataSupport.buildLitigiousParty(
                caseData.getApplicant1(),
                caseData.getApplicant1LitigationFriend(),
                "Claimant",
                APPLICANT_ID,
                APPLICANT_SOLICITOR_ID,
                caseDataSupport.organisationId(caseData.getApplicant1OrganisationPolicy()).orElse(null),
                dateOfService
            ),
            caseDataSupport.buildLitigiousParty(
                caseData.getRespondent1(),
                caseData.getRespondent1LitigationFriend(),
                "Defendant",
                RESPONDENT_ID,
                respondent1SolicitorId,
                caseDataSupport.organisationId(caseData.getRespondent1OrganisationPolicy()).orElse(null),
                dateOfService
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
                dateOfService
            ));
        }

        if (caseData.getRespondent2() != null) {
            String respondent2SolicitorId = null;
            if (caseData.getSpecRespondent2Represented() == YES
                && caseData.getRespondent2SameLegalRepresentative() == YES) {
                respondent2SolicitorId = RESPONDENT_SOLICITOR_ID;
            } else if (caseData.getSpecRespondent2Represented() == YES
                && caseData.getRespondent2SameLegalRepresentative() == NO) {
                respondent2SolicitorId = RESPONDENT2_SOLICITOR_ID;
            }
            respondentParties.add(caseDataSupport.buildLitigiousParty(
                caseData.getRespondent2(),
                caseData.getRespondent2LitigationFriend(),
                "Defendant",
                RESPONDENT2_ID,
                respondent2SolicitorId,
                caseDataSupport.organisationId(caseData.getRespondent2OrganisationPolicy()).orElse(null),
                dateOfService
            ));
        }
        return respondentParties;
    }
}
