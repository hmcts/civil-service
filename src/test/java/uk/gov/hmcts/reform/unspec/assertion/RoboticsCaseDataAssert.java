package uk.gov.hmcts.reform.unspec.assertion;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.LitigationFriend;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.unspec.model.robotics.CaseHeader;
import uk.gov.hmcts.reform.unspec.model.robotics.ClaimDetails;
import uk.gov.hmcts.reform.unspec.model.robotics.LitigiousParty;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.unspec.model.robotics.Solicitor;
import uk.gov.hmcts.reform.unspec.utils.PartyUtils;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.unspec.assertion.CustomAssertions.assertMoney;
import static uk.gov.hmcts.reform.unspec.assertion.CustomAssertions.assertThat;
import static uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper.APPLICANT_SOLICITOR_ID;
import static uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper.RESPONDENT_SOLICITOR_ID;

public class RoboticsCaseDataAssert extends CustomAssert<RoboticsCaseDataAssert, RoboticsCaseData> {

    public RoboticsCaseDataAssert(RoboticsCaseData actual) {
        super("RoboticsCaseData", actual, RoboticsCaseDataAssert.class);
    }

    public RoboticsCaseDataAssert isEqualTo(CaseData expected) {
        isNotNull();

        CaseHeader header = actual.getHeader();
        assertHeader(expected, header);

        assertClaimDetails(expected, actual.getClaimDetails());

        assertParty(
            "applicant1",
            "Claimant",
            actual.getLitigiousParties().get(0),
            expected.getApplicant1(),
            expected.getApplicant1LitigationFriend()
        );
        assertParty(
            "respondent1",
            "Defendant",
            actual.getLitigiousParties().get(1),
            expected.getRespondent1(),
            expected.getRespondent1LitigationFriend()
        );

        assertSolicitor(
            APPLICANT_SOLICITOR_ID,
            "applicant1" + "." + "reference",
            actual.getSolicitors().get(0),
            expected.getSolicitorReferences().getApplicantSolicitor1Reference(),
            null,
            expected.getApplicant1OrganisationPolicy()
        );
        if (actual.getSolicitors().size() == 2) {
            assertSolicitor(
                RESPONDENT_SOLICITOR_ID,
                "respondent1" + "." + "reference",
                actual.getSolicitors().get(1),
                expected.getSolicitorReferences().getRespondentSolicitor1Reference(),
                expected.getRespondentSolicitor1OrganisationDetails(),
                expected.getRespondent1OrganisationPolicy()
            );
        }

        assertNotNull(actual.getEvents());

        return this;
    }

    private void assertHeader(CaseData expected, CaseHeader header) {
        compare(
            "caseNumber",
            expected.getLegacyCaseReference(),
            ofNullable(header.getCaseNumber())
        );
        compare(
            "caseType",
            "PERSONAL INJURY",
            ofNullable(header.getCaseType())
        );
        compare(
            "owningCourtCode",
            "390",
            ofNullable(header.getOwningCourtCode())
        );
        compare(
            "owningCourtName",
            "CCMCC",
            ofNullable(header.getOwningCourtName())
        );
        compare(
            "preferredCourtCode",
            expected.getCourtLocation().getApplicantPreferredCourt(),
            ofNullable(header.getPreferredCourtCode())
        );
    }

    private void assertClaimDetails(CaseData expected, ClaimDetails actual) {
        compare(
            "caseIssuedDate",
            ofNullable(expected.getIssueDate())
                .map(date -> date.format(ISO_DATE))
                .orElse(null),
            ofNullable(actual.getCaseIssuedDate())
        );

        compare(
            "caseRequestReceivedDate",
            expected.getSubmittedDate().toLocalDate().format(ISO_DATE),
            ofNullable(actual.getCaseRequestReceivedDate())
        );

        compare(
            "amountClaimed",
            expected.getClaimValue().toPounds(),
            ofNullable(actual.getAmountClaimed())
        );

        compare(
            "courtFee",
            ofNullable(expected.getClaimFee())
                .map(fee -> fee.getCalculatedAmountInPence())
                .orElse(null),
            ofNullable(actual.getCourtFee()),
            (e, a) -> assertMoney(a).isEqualTo(e)
        );
    }

    private void assertSolicitor(String id, String fieldName, Solicitor solicitor, String reference,
                                 SolicitorOrganisationDetails solicitorOrganisationDetails,
                                 OrganisationPolicy organisationPolicy
    ) {
        compare(
            "id",
            solicitor.getId(),
            ofNullable(id)
        );
        compare(
            fieldName,
            solicitor.getReference(),
            ofNullable(reference)
        );
        ofNullable(solicitorOrganisationDetails)
            .ifPresent(organisationDetails -> {
                compare(
                    "name",
                    solicitor.getName(),
                    ofNullable(organisationDetails.getOrganisationName())
                );
                compare(
                    "contactTelephoneNumber",
                    solicitor.getContactTelephoneNumber(),
                    ofNullable(organisationDetails.getPhoneNumber())
                );
                compare(
                    "contactFaxNumber",
                    solicitor.getContactFaxNumber(),
                    ofNullable(organisationDetails.getFax())
                );
                compare(
                    "contactDX",
                    solicitor.getContactDX(),
                    ofNullable(organisationDetails.getDx())
                );
                compare(
                    "contactEmailAddress",
                    solicitor.getContactEmailAddress(),
                    ofNullable(organisationDetails.getEmail())
                );
                assertThat(solicitor.getAddresses().getContactAddress())
                    .isEqualTo(organisationDetails.getAddress());
            });

        ofNullable(organisationPolicy)
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .ifPresent(organisationId ->
                           compare(
                               "organisationId",
                               solicitor.getOrganisationId(),
                               ofNullable(organisationId)
                           )
            );
    }

    private void assertParty(String fieldName,
                             String litigiousPartyType,
                             LitigiousParty litigiousParty,
                             Party party,
                             LitigationFriend litigationFriend
    ) {
        if (party == null && litigiousParty != null) {
            failExpectedPresent(fieldName, litigiousParty);
            return;
        }

        if (party != null && litigiousParty == null) {
            failExpectedAbsent(fieldName, party);
            return;
        }

        compare(
            "name",
            litigiousParty.getName(),
            ofNullable(PartyUtils.getLitigiousPartyName(party, litigationFriend))
        );
        compare(
            "type",
            litigiousParty.getType(),
            ofNullable(litigiousPartyType)
        );
        compare(
            "dateOfBirth",
            litigiousParty.getDateOfBirth(),
            PartyUtils.getDateOfBirth(party).map(d -> d.format(ISO_DATE))
        );

        assertThat(litigiousParty.getAddresses().getContactAddress())
            .isEqualTo(party.getPrimaryAddress());
    }
}
