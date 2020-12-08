package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
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

/**
 * This class is skeleton to be refined after we have final version of RPA Json structure
 * and it's mapping with CaseData.
 */
@Service
@RequiredArgsConstructor
public class RoboticsDataMapper {

    private final RoboticsAddressMapper addressMapper;

    public RoboticsCaseData toRoboticsCaseData(CaseData caseData) {
        requireNonNull(caseData);
        return RoboticsCaseData.builder()
            .header(buildCaseHeader(caseData))
            .litigiousParties(buildLitigiousParties(caseData))
            .solicitors(buildSolicitors(caseData))
            .claimDetails(buildClaimDetails(caseData.getClaimValue()))
            .build();
    }

    private ClaimDetails buildClaimDetails(ClaimValue claimValue) {
        return ClaimDetails.builder()
            .amountClaimed(claimValue.getStatementOfValueInPennies())
            .build();
    }

    private CaseHeader buildCaseHeader(CaseData caseData) {
        return CaseHeader.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .preferredCourtName(caseData.getCourtLocation().getApplicantPreferredCourt())
            .build();
    }

    private List<Solicitor> buildSolicitors(CaseData caseData) {
        return List.of(buildApplicantSolicitor(caseData), buildRespondentSolicitor(caseData));
    }

    private Solicitor buildRespondentSolicitor(CaseData caseData) {
        return Solicitor.builder()
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getRespondentSolicitor1Reference)
                           .orElse(null)
            )
            .build();
    }

    private Solicitor buildApplicantSolicitor(CaseData caseData) {
        return Solicitor.builder()
            .reference(ofNullable(caseData.getSolicitorReferences())
                           .map(SolicitorReferences::getApplicantSolicitor1Reference)
                           .orElse(null)
            )
            .build();
    }

    private List<LitigiousParty> buildLitigiousParties(CaseData caseData) {
        return List.of(buildLitigiousParty(caseData.getApplicant1()), buildLitigiousParty(caseData.getRespondent1()));
    }

    private LitigiousParty buildLitigiousParty(Party party) {
        return LitigiousParty.builder()
            .name(party.getPartyName())
            .type(party.getType().getDisplayValue())
            .dateOfBirth(PartyUtils.getDateOfBirth(party).map(d -> d.format(ISO_DATE)).orElse(null))
            .addresses(addressMapper.toRoboticsAddresses(party.getPrimaryAddress()))
            .build();
    }
}
