package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
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
import uk.gov.hmcts.reform.civil.model.robotics.SolicitorSpec;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.DxAddress;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.MonetaryConversions.penniesToPounds;

/**
 * This class is skeleton to be refined after we have final version of RPA Json structure
 * and it's mapping with CaseData.
 */
@Service
@RequiredArgsConstructor
public class RoboticsDataMapperForSpec {

    public static final String APPLICANT_SOLICITOR_ID = "001";
    public static final String RESPONDENT_SOLICITOR_ID = "002";
    public static final String APPLICANT_ID = "001";
    public static final String RESPONDENT_ID = "002";

    private final RoboticsAddressMapper addressMapper;
    private final EventHistoryMapper eventHistoryMapper;
    private final OrganisationService organisationService;

    public RoboticsCaseDataSpec toRoboticsCaseData(CaseData caseData) {
        requireNonNull(caseData);
        return RoboticsCaseDataSpec.builder()
            .header(buildCaseHeader(caseData))
            .litigiousParties(buildLitigiousParties(caseData))
            .solicitors(buildSolicitors(caseData))
            .claimDetails(buildClaimDetails(caseData))
            .events(eventHistoryMapper.buildEvents(caseData))
            .build();
    }

    private ClaimDetails buildClaimDetails(CaseData caseData) {
        BigDecimal claimInterest = caseData.getTotalInterest() != null
            ? caseData.getTotalInterest() : null;
        return ClaimDetails.builder()
            .amountClaimed(caseData.getTotalClaimAmount())
            .totalInterest(claimInterest)
            .totalClaimAmountWithInterest(claimInterest != null
                                              ? caseData.getTotalClaimAmount().add(claimInterest)
                                              : caseData.getTotalClaimAmount())
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
            .owningCourtCode("")
            .owningCourtName("")
            .caseType("SPECIFIED CLAIM")
            .preferredCourtCode("")
            .caseAllocatedTo("")
            .build();
    }

    private List<SolicitorSpec> buildSolicitors(CaseData caseData) {
        List<SolicitorSpec> solicitorsList = new ArrayList<>();
        solicitorsList.add(buildApplicantSolicitor(caseData, APPLICANT_SOLICITOR_ID));
        ofNullable(buildRespondentSolicitor(caseData, RESPONDENT_SOLICITOR_ID))
            .ifPresent(solicitorsList::add);

        return solicitorsList;
    }

    private SolicitorSpec buildRespondentSolicitor(CaseData caseData, String id) {
        SolicitorSpec.SolicitorSpecBuilder solicitorSpecBuilder = SolicitorSpec.builder();
        Optional<String> organisationId = getOrganisationId(caseData.getRespondent1OrganisationPolicy());
        var organisationDetails = ofNullable(
            caseData.getRespondentSolicitor1OrganisationDetails()
        );
        if (organisationId.isEmpty() && organisationDetails.isEmpty()) {
            return null;
        }
        solicitorSpecBuilder
            .id(id)
            .isPayee(false)
            .organisationId(organisationId.orElse(null))
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getRespondentSolicitor1Reference)
                           .orElse(null)
            );
        solicitorSpecBuilder
            .correspondenceAddresses(caseData.getRespondentSolicitor1ServiceAddress() != null
                                         ? addressMapper.toRoboticsAddresses(
                caseData.getRespondentSolicitor1ServiceAddress()) : null);

        organisationId
            .flatMap(organisationService::findOrganisationById)
            .ifPresent(buildOrganisation(solicitorSpecBuilder, null));

        organisationDetails.ifPresent(buildOrganisationDetails(solicitorSpecBuilder));

        return solicitorSpecBuilder.build();
    }

    private Consumer<uk.gov.hmcts.reform.prd.model.Organisation> buildOrganisation(
        SolicitorSpec.SolicitorSpecBuilder solicitorSpecBuilder, Address providedServiceAddress
    ) {
        return organisation -> {
            List<ContactInformation> contactInformation = organisation.getContactInformation();
            solicitorSpecBuilder
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
        SolicitorSpec.SolicitorSpecBuilder solicitorSpecBuilder
    ) {
        return organisationDetails ->
            solicitorSpecBuilder
                .name(organisationDetails.getOrganisationName())
                .contactTelephoneNumber(organisationDetails.getPhoneNumber())
                .contactFaxNumber(organisationDetails.getFax())
                .contactDX(organisationDetails.getDx())
                .contactEmailAddress(organisationDetails.getEmail())
                .addresses(addressMapper.toRoboticsAddresses(organisationDetails.getAddress()));
    }

    private SolicitorSpec buildApplicantSolicitor(CaseData caseData, String id) {
        Optional<String> organisationId = getOrganisationId(caseData.getApplicant1OrganisationPolicy());
        var providedServiceAddress = caseData.getApplicantSolicitor1ServiceAddress();
        SolicitorSpec.SolicitorSpecBuilder solicitorSpecBuilder = SolicitorSpec.builder()
            .id(id)
            .isPayee(true)
            .organisationId(organisationId.orElse(null))
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getApplicantSolicitor1Reference)
                           .orElse(null)
            )
            .correspondenceAddresses(caseData.getSpecApplicantCorrespondenceAddressdetails() != null
                                         ? addressMapper.toRoboticsAddresses(
                caseData.getSpecApplicantCorrespondenceAddressdetails()) : null);

        organisationId
            .flatMap(organisationService::findOrganisationById)
            .ifPresent(buildOrganisation(solicitorSpecBuilder, caseData.getApplicantSolicitor1ServiceAddress()));

        return solicitorSpecBuilder.build();
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
            .solicitorOrganisationID(getOrganisationId(organisationPolicy).orElse(null))
            .build();
    }
}
