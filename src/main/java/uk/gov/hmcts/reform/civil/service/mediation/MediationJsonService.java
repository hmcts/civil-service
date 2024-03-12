package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.PartyFlags;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveCaseLevelFlags;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveFlags;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@AllArgsConstructor
public class MediationJsonService {

    private final OrganisationService organisationService;

    private static final String PAPER_RESPONSE = "N";

    public MediationCase generateJsonContent(CaseData caseData) {
        List<MediationLitigant> litigantList = new ArrayList<>();

        // case flags check
        List<FlagDetail> allActiveCaseLevelFlags = getAllActiveCaseLevelFlags(caseData);
        caseData.getRespondent1DQ().getWelshLanguageRequirementsLRspec();

        buildLitigantFields(caseData, litigantList);

        return MediationCase.builder()
            .litigants(litigantList)
            .build();
    }

    private void buildLitigantFields(CaseData caseData, List<MediationLitigant> litigantList) {
        litigantList.add(buildApplicant1Fields(caseData));
        litigantList.add(buildApplicant2Fields(caseData));
        litigantList.add(buildRespondent1Fields(caseData));
        litigantList.add(buildRespondent2Fields(caseData));
    }

    private MediationLitigant buildApplicant1Fields(CaseData caseData) {
        if (NO.equals(caseData.getApplicant1Represented())) {
            return buildUnrepresentedLitigant(caseData.getApplicant1());
        } else {
            return buildRepresentedLitigant(caseData.getApplicant1(), caseData.getApplicant1OrganisationPolicy(),
                                                      caseData.getApplicantSolicitor1UserDetails().getEmail());
        }
    }

    private MediationLitigant buildApplicant2Fields(CaseData caseData) {
        if (caseData.getApplicant2() != null) {
            return buildRepresentedLitigant(caseData.getApplicant2(), caseData.getApplicant2OrganisationPolicy(),
                                            caseData.getApplicantSolicitor1UserDetails().getEmail());
            }
        return null;
    }

    private MediationLitigant buildRespondent1Fields(CaseData caseData) {
        if (NO.equals(caseData.getRespondent1Represented())) {
            return buildUnrepresentedLitigant(caseData.getRespondent1());
        } else {
            return buildRepresentedLitigant(caseData.getRespondent1(), caseData.getRespondent1OrganisationPolicy(),
                                            caseData.getRespondentSolicitor1EmailAddress());
        }
    }

    private MediationLitigant buildRespondent2Fields(CaseData caseData) {
        if (caseData.getRespondent2() != null) {
            if (NO.equals(caseData.getRespondent2Represented())) {
                return buildUnrepresentedLitigant(caseData.getRespondent2());
            } else {
                return buildRepresentedLitigant(caseData.getRespondent2(), caseData.getRespondent2OrganisationPolicy(),
                                                caseData.getRespondentSolicitor2EmailAddress());
            }
        }
        return null;
    }

    private MediationLitigant buildRepresentedLitigant(Party party, OrganisationPolicy organisationPolicy, String solicitorEmail) {
        String orgId = organisationPolicy.getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(orgId);
        Organisation solicitorOrgDetails = organisation.orElse(null);

        return MediationLitigant.builder()
            .partyID(party.getPartyID())
            .partyRole(party.getFlags().getRoleOnCase())
            .partyType(party.getType())
            .partyName(party.getPartyName())
            .paperResponse(PAPER_RESPONSE)
            .represented(true)
            .solicitorOrgName(solicitorOrgDetails != null ? solicitorOrgDetails.getName() : null)
            .litigantEmail(solicitorEmail)
            .litigantTelephone(solicitorOrgDetails != null ? solicitorOrgDetails.getCompanyNumber() : null)
            .mediationContactName("")
            .mediationContactNumber("")
            .mediationContactEmail("")
            .dateRangeToAvoid(List.of(UnavailableDate.builder().build()))
            .build();
    }

    private MediationLitigant buildUnrepresentedLitigant(Party party) {

        return MediationLitigant.builder()
            .partyID(party.getPartyID())
            .partyRole(party.getFlags().getRoleOnCase())
            .partyType(party.getType())
            .partyName(party.getPartyName())
            .paperResponse(PAPER_RESPONSE)
            .represented(false)
            .solicitorOrgName(null)
            .litigantEmail(party.getPartyEmail())
            .litigantTelephone(party.getPartyPhone())
            .mediationContactName("")
            .mediationContactNumber("")
            .mediationContactEmail("")
            .dateRangeToAvoid(List.of(UnavailableDate.builder().build()))
            .build();
    }
}
