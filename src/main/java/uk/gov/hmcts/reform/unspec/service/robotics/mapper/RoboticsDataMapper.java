package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.LitigationFriend;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.robotics.CaseHeader;
import uk.gov.hmcts.reform.unspec.model.robotics.ClaimDetails;
import uk.gov.hmcts.reform.unspec.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.unspec.model.robotics.Solicitor;
import uk.gov.hmcts.reform.unspec.utils.PartyUtils;

import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.utils.MonetaryConversions.penniesToPounds;

/**
 * This class is skeleton to be refined after we have final version of RPA Json structure
 * and it's mapping with CaseData.
 */
@Service
@RequiredArgsConstructor
public class RoboticsDataMapper {

    public static final String APPLICANT_SOLICITOR_ID = "001";
    public static final String RESPONDENT_SOLICITOR_ID = "002";
    public static final String APPLICANT_ID = "001";
    public static final String RESPONDENT_ID = "002";

    private final RoboticsAddressMapper addressMapper;
    private final EventHistoryMapper eventHistoryMapper;

    public RoboticsCaseData toRoboticsCaseData(CaseData caseData) {
        requireNonNull(caseData);
        return RoboticsCaseData.builder()
            .header(buildCaseHeader(caseData))
            .litigiousParties(buildLitigiousParties(caseData))
            .solicitors(buildSolicitors(caseData))
            .claimDetails(buildClaimDetails(caseData))
            .events(eventHistoryMapper.buildEvents(caseData))
            .build();
    }

    private ClaimDetails buildClaimDetails(CaseData caseData) {
        return ClaimDetails.builder()
            .amountClaimed(caseData.getClaimValue().toPounds())
            .courtFee(ofNullable(caseData.getClaimFee())
                          .map(fee -> penniesToPounds(fee.getCalculatedAmountInPence()))
                          .orElse(null))
            .caseIssuedDate(ofNullable(caseData.getClaimIssuedDate())
                                .map(issueDate -> issueDate.format(ISO_DATE))
                                .orElse(null))
            .caseRequestReceivedDate(caseData.getClaimSubmittedDateTime().toLocalDate().format(ISO_DATE))
            .build();
    }

    private CaseHeader buildCaseHeader(CaseData caseData) {
        return CaseHeader.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .owningCourtCode("390")
            .owningCourtName("CCMCC")
            .caseType("PERSONAL INJURY")
            .preferredCourtCode(caseData.getCourtLocation().getApplicantPreferredCourt())
            .caseAllocatedTo(buildAllocatedTrack(caseData.getAllocatedTrack()))
            .build();
    }

    private String buildAllocatedTrack(AllocatedTrack allocatedTrack) {
        switch (allocatedTrack) {
            case FAST_CLAIM:
                return "FAST TRACK";
            case MULTI_CLAIM:
                return "MULTI TRACK";
            case SMALL_CLAIM:
                return "SMALL CLAIM TRACK";
            default:
                return "";
        }
    }

    private List<Solicitor> buildSolicitors(CaseData caseData) {
        return List.of(
            buildApplicantSolicitor(caseData, APPLICANT_SOLICITOR_ID),
            buildRespondentSolicitor(caseData, RESPONDENT_SOLICITOR_ID)
        );
    }

    private Solicitor buildRespondentSolicitor(CaseData caseData, String id) {
        return Solicitor.builder()
            .id(id)
            .organisationId(ofNullable(caseData.getRespondent1OrganisationPolicy())
                                .map(organisationPolicy -> organisationPolicy.getOrganisation().getOrganisationID())
                                .orElse(null)
            )
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getRespondentSolicitor1Reference)
                           .orElse(null)
            )
            .build();
    }

    private Solicitor buildApplicantSolicitor(CaseData caseData, String id) {
        return Solicitor.builder()
            .id(id)
            .organisationId(ofNullable(caseData.getApplicant1OrganisationPolicy())
                                .map(organisationPolicy -> organisationPolicy.getOrganisation().getOrganisationID())
                                .orElse(null)
            )
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getApplicantSolicitor1Reference)
                           .orElse(null)
            )
            .build();
    }

    private List<LitigiousParty> buildLitigiousParties(CaseData caseData) {
        return List.of(
            buildLitigiousParty(
                caseData.getApplicant1(),
                caseData.getApplicant1LitigationFriend(),
                caseData.getApplicant1OrganisationPolicy(),
                "Claimant",
                APPLICANT_ID,
                APPLICANT_SOLICITOR_ID
            ),
            buildLitigiousParty(
                caseData.getRespondent1(),
                caseData.getRespondent1LitigationFriend(),
                caseData.getRespondent1OrganisationPolicy(),
                "Defendant",
                RESPONDENT_ID,
                RESPONDENT_SOLICITOR_ID
            )
        );
    }

    private LitigiousParty buildLitigiousParty(
        Party party,
        LitigationFriend litigationFriend,
        OrganisationPolicy organisationPolicy,
        String type,
        String id,
        String solicitorId
    ) {
        return LitigiousParty.builder()
            .id(id)
            .solicitorID(solicitorId)
            .type(type)
            .name(PartyUtils.getLitigiousPartyName(party, litigationFriend))
            .dateOfBirth(PartyUtils.getDateOfBirth(party).map(d -> d.format(ISO_DATE)).orElse(null))
            .addresses(addressMapper.toRoboticsAddresses(party.getPrimaryAddress()))
            .solicitorOrganisationID(getOrganisationID(organisationPolicy))
            .build();
    }

    private String getOrganisationID(OrganisationPolicy organisationPolicy) {
        return ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse(null);
    }
}
