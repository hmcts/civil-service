package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.robotics.CaseHeader;
import uk.gov.hmcts.reform.civil.model.robotics.ClaimDetails;
import uk.gov.hmcts.reform.civil.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddresses;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.model.robotics.Solicitor;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jsonwebtoken.lang.Collections.isEmpty;
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

/**
 * This class is skeleton to be refined after we have final version of RPA Json structure
 * and it's mapping with CaseData.
 */
@Service
@RequiredArgsConstructor
public class RoboticsDataMapperForSpec {

    private final RoboticsAddressMapper addressMapper;
    private final EventHistoryMapper eventHistoryMapper;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;

    public RoboticsCaseDataSpec toRoboticsCaseData(CaseData caseData) {
        requireNonNull(caseData);
        RoboticsCaseDataSpec.RoboticsCaseDataSpecBuilder builder = RoboticsCaseDataSpec.builder()
            .header(buildCaseHeader(caseData))
            .litigiousParties(buildLitigiousParties(caseData))
            .solicitors(buildSolicitors(caseData))
            .claimDetails(buildClaimDetails(caseData))
            .events(eventHistoryMapper.buildEvents(caseData));

        if (featureToggleService.isNoticeOfChangeEnabled()
            && (caseData.getCcdState() == PROCEEDS_IN_HERITAGE_SYSTEM
            || caseData.getCcdState() == CASE_DISMISSED)) {
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
            .courtFee(ofNullable(caseData.getClaimFee())
                          .map(fee -> penniesToPounds(fee.getCalculatedAmountInPence()))
                          .orElse(null))
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
        solicitorsList.add(buildApplicantSolicitor(caseData));
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
        solicitorBuilder
            .id(RoboticsDataUtil.RESPONDENT_SOLICITOR_ID)
            .isPayee(false)
            .organisationId(organisationId.orElse(null))
            .contactEmailAddress(solicitorEmail.orElse(null))
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getRespondentSolicitor1Reference)
                           .map(s -> s.substring(0, Math.min(s.length(), 24)))
                           .orElse(null)
            );
        organisationId
            .flatMap(organisationService::findOrganisationById)
            .ifPresent(buildOrganisation(solicitorBuilder,
                                         caseData.getSpecRespondentCorrespondenceAddressdetails()));

        organisationDetails.ifPresent(buildOrganisationDetails(solicitorBuilder));

        return solicitorBuilder.build();
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
        solicitorBuilder
            .id(RoboticsDataUtil.RESPONDENT2_SOLICITOR_ID)
            .isPayee(false)
            .organisationId(organisationId.orElse(null))
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getRespondentSolicitor2Reference)
                           .map(s -> s.substring(0, Math.min(s.length(), 24)))
                           .orElse(null)
            );

        organisationId
            .flatMap(organisationService::findOrganisationById)
            .ifPresent(buildOrganisation(solicitorBuilder,
                                         caseData.getSpecRespondent2CorrespondenceAddressdetails()));

        organisationDetails.ifPresent(buildOrganisationDetails(solicitorBuilder));

        return solicitorBuilder.build();
    }

    private Consumer<uk.gov.hmcts.reform.civil.prd.model.Organisation> buildOrganisation(
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder, Address providedServiceAddress
    ) {
        return organisation -> {
            List<ContactInformation> contactInformation = organisation.getContactInformation();
            solicitorBuilder
                .name(organisation.getName())
                .addresses(fromProvidedAddress(contactInformation, providedServiceAddress))
                .contactDX(getContactDX(contactInformation));
        };
    }

    private RoboticsAddresses fromProvidedAddress(List<ContactInformation> contactInformation, Address provided) {
        return Optional.ofNullable(provided)
            .map(address -> addressMapper.toRoboticsAddresses(provided))
            .orElse(addressMapper.toRoboticsAddresses(contactInformation));
    }

    private String getContactDX(List<ContactInformation> contactInformation) {
        if (isEmpty(contactInformation)) {
            return null;
        }
        List<DxAddress> dxAddresses = contactInformation.get(0).getDxAddress();
        return isEmpty(dxAddresses) ? null : dxAddresses.get(0).getDxNumber();
    }

    private Optional<String> getOrganisationId(OrganisationPolicy respondent1OrganisationPolicy) {
        return ofNullable(respondent1OrganisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID);
    }

    private Consumer<SolicitorOrganisationDetails> buildOrganisationDetails(
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder
    ) {
        return organisationDetails ->
            solicitorBuilder
                .name(organisationDetails.getOrganisationName())
                .contactTelephoneNumber(organisationDetails.getPhoneNumber())
                .contactFaxNumber(organisationDetails.getFax())
                .contactDX(organisationDetails.getDx())
                .contactEmailAddress(organisationDetails.getEmail())
                .addresses(addressMapper.toRoboticsAddresses(organisationDetails.getAddress()));
    }

    private Solicitor buildApplicantSolicitor(CaseData caseData) {
        Optional<String> organisationId = getOrganisationId(caseData.getApplicant1OrganisationPolicy());
        var providedServiceAddress = caseData.getSpecApplicantCorrespondenceAddressdetails();
        Solicitor.SolicitorBuilder<?, ?> solicitorBuilder = Solicitor.builder()
            .id(RoboticsDataUtil.APPLICANT_SOLICITOR_ID)
            .isPayee(true)
            .organisationId(organisationId.orElse(null))
            .contactEmailAddress(caseData.getApplicantSolicitor1UserDetails().getEmail())
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getApplicantSolicitor1Reference)
                           .map(s -> s.substring(0, Math.min(s.length(), 24)))
                           .orElse(null)
            );

        organisationId
            .flatMap(organisationService::findOrganisationById)
            .ifPresent(buildOrganisation(solicitorBuilder, providedServiceAddress));

        return solicitorBuilder.build();
    }

    private List<LitigiousParty> buildLitigiousParties(CaseData caseData) {
        LocalDate dateOfService = caseData.getIssueDate();
        String respondent1SolicitorId = caseData.getSpecRespondent1Represented() == YES
            ? RESPONDENT_SOLICITOR_ID : null;

        var respondentParties = new ArrayList<>(List.of(
            buildLitigiousParty(
                caseData.getApplicant1(),
                caseData.getApplicant1LitigationFriend(),
                caseData.getApplicant1OrganisationPolicy(),
                "Claimant",
                APPLICANT_ID,
                APPLICANT_SOLICITOR_ID,
                dateOfService
            ),
            buildLitigiousParty(
                caseData.getRespondent1(),
                caseData.getRespondent1LitigationFriend(),
                caseData.getRespondent1OrganisationPolicy(),
                "Defendant",
                RESPONDENT_ID,
                respondent1SolicitorId,
                dateOfService
            )
        ));

        if (caseData.getApplicant2() != null) {
            respondentParties.add(buildLitigiousParty(
                caseData.getApplicant2(),
                caseData.getApplicant2LitigationFriend(),
                caseData.getApplicant2OrganisationPolicy(),
                "Claimant",
                APPLICANT2_ID,
                APPLICANT_SOLICITOR_ID,
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
            respondentParties.add(buildLitigiousParty(
                caseData.getRespondent2(),
                caseData.getRespondent2LitigationFriend(),
                caseData.getRespondent2OrganisationPolicy(),
                "Defendant",
                RESPONDENT2_ID,
                respondent2SolicitorId,
                dateOfService
            ));
        }
        return respondentParties;
    }

    private LitigiousParty buildLitigiousParty(
        Party party,
        LitigationFriend litigationFriend,
        OrganisationPolicy organisationPolicy,
        String type,
        String id,
        String solicitorId,
        LocalDate dateOfService
    ) {
        return LitigiousParty.builder()
            .id(id)
            .solicitorID(solicitorId)
            .type(type)
            .name(PartyUtils.getLitigiousPartyName(party, litigationFriend))
            .dateOfBirth(PartyUtils.getDateOfBirth(party).map(d -> d.format(ISO_DATE)).orElse(null))
            .addresses(addressMapper.toRoboticsAddresses(party.getPrimaryAddress()))
            .solicitorOrganisationID(getOrganisationId(organisationPolicy).orElse(null))
            .dateOfService(ofNullable(dateOfService)
                               .map(d -> d.format(ISO_DATE))
                               .orElse(null))
            .build();
    }
}
