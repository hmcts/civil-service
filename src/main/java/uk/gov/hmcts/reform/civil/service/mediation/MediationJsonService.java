package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.VulnerabilityQuestions;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveCaseLevelFlags;

@Slf4j
@Service
@AllArgsConstructor
public class MediationJsonService {

    private final OrganisationService organisationService;

    private static final String PAPER_RESPONSE = "N";

    public MediationCase generateJsonContent(CaseData caseData) {
        List<MediationLitigant> litigantList = new ArrayList<>();

        // caseFlags
        boolean activeCaseFlags = buildCaseFlags(caseData);

        // litigants
        buildLitigantFields(caseData, litigantList);

        return MediationCase.builder()
            .ccdCaseNumber(caseData.getCcdCaseReference())
            .caseFlags(activeCaseFlags)
            .litigants(litigantList)
            .build();
    }

    private boolean buildCaseFlags(CaseData caseData) {
        return checkForActiveCaseLevelFlags(caseData) || checkApplicant1DQRequirements(caseData)
            || checkRespondent1DQRequirements(caseData) || checkRespondent2DQRequirements(caseData)
            || checkApplicant2DQRequirements(caseData);
    }

    private boolean checkApplicant1DQRequirements(CaseData caseData) {
        if (caseData.getApplicant1DQ() != null) {
            Applicant1DQ applicant1DQ = caseData.getApplicant1DQ();

            return welshOrBilingualLanguage(applicant1DQ.getWelshLanguageRequirements())
                || vulnerabilityAdjustmentRequired(applicant1DQ.getVulnerabilityQuestions())
                || supportWithAccessNeedsRequired(applicant1DQ.getApplicant1DQHearingSupport());
        }
        return false;
    }

    private boolean checkApplicant2DQRequirements(CaseData caseData) {
        if (caseData.getApplicant2DQ() != null) {
            Applicant2DQ applicant2DQ = caseData.getApplicant2DQ();

            return welshOrBilingualLanguage(applicant2DQ.getWelshLanguageRequirements())
                || vulnerabilityAdjustmentRequired(applicant2DQ.getVulnerabilityQuestions())
                || supportWithAccessNeedsRequired(applicant2DQ.getApplicant2DQHearingSupport());
        }
        return false;
    }

    private boolean checkRespondent1DQRequirements(CaseData caseData) {
        if (caseData.getRespondent1DQ() != null) {
            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();

            return welshOrBilingualLanguage(respondent1DQ.getWelshLanguageRequirements())
                || vulnerabilityAdjustmentRequired(respondent1DQ.getVulnerabilityQuestions())
                || supportWithAccessNeedsRequired(respondent1DQ.getRespondent1DQHearingSupport());
        }
        return false;
    }

    private boolean checkRespondent2DQRequirements(CaseData caseData) {
        if (caseData.getRespondent2DQ() != null) {
            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();

            return welshOrBilingualLanguage(respondent2DQ.getWelshLanguageRequirements())
                || vulnerabilityAdjustmentRequired(respondent2DQ.getVulnerabilityQuestions())
                || supportWithAccessNeedsRequired(respondent2DQ.getRespondent2DQHearingSupport());
        }
        return false;
    }

    private boolean supportWithAccessNeedsRequired(HearingSupport hearingSupport) {
        return hearingSupport != null && YES.equals(hearingSupport.getSupportRequirements());
    }

    private boolean vulnerabilityAdjustmentRequired(VulnerabilityQuestions vulnerabilityQuestions) {
        return vulnerabilityQuestions != null && YES.equals(vulnerabilityQuestions.getVulnerabilityAdjustmentsRequired());
    }

    private boolean welshOrBilingualLanguage(WelshLanguageRequirements welshLanguageRequirements) {
        boolean languageFlags = false;
        if (welshLanguageRequirements != null) {
            if (WELSH.equals(welshLanguageRequirements.getCourt()) || BOTH.equals(welshLanguageRequirements.getCourt())) {
                languageFlags = true;
            }
            if (WELSH.equals(welshLanguageRequirements.getDocuments()) || BOTH.equals(welshLanguageRequirements.getDocuments())) {
                languageFlags = true;
            }
            if (WELSH.equals(welshLanguageRequirements.getEvidence()) || BOTH.equals(welshLanguageRequirements.getEvidence())) {
                languageFlags = true;
            }
        }
        return languageFlags;
    }

    private boolean checkForActiveCaseLevelFlags(CaseData caseData) {
        List<FlagDetail> allActiveCaseLevelFlags = getAllActiveCaseLevelFlags(caseData);

        return !allActiveCaseLevelFlags.isEmpty();
    }

    private void buildLitigantFields(CaseData caseData, List<MediationLitigant> litigantList) {
        litigantList.add(buildApplicant1Fields(caseData));
        if (caseData.getApplicant2() != null) {
            litigantList.add(buildApplicant2Fields(caseData));
        }
        litigantList.add(buildRespondent1Fields(caseData));
        if (caseData.getRespondent2() != null) {
            litigantList.add(buildRespondent2Fields(caseData));
        }
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
        return buildRepresentedLitigant(caseData.getApplicant2(), caseData.getApplicant2OrganisationPolicy() != null
                                            ? caseData.getApplicant2OrganisationPolicy()
                                            : caseData.getApplicant1OrganisationPolicy(),
                                        caseData.getApplicantSolicitor1UserDetails().getEmail());
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
        if (NO.equals(caseData.getRespondent2Represented())) {
            return buildUnrepresentedLitigant(caseData.getRespondent2());
        } else {
            return buildRepresentedLitigant(caseData.getRespondent2(), caseData.getRespondent2OrganisationPolicy(),
                                            caseData.getRespondentSolicitor2EmailAddress());
        }
    }

    private MediationLitigant buildRepresentedLitigant(Party party, OrganisationPolicy organisationPolicy, String solicitorEmail) {
        String orgId = organisationPolicy.getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(orgId);
        Organisation solicitorOrgDetails = organisation.orElse(null);
        if (solicitorOrgDetails != null) {
            log.info("solicitor details: {} {} {} {} ", solicitorOrgDetails.getName(),
                    solicitorOrgDetails.getCompanyNumber(),
                    solicitorOrgDetails.getCompanyUrl(),
                    solicitorOrgDetails.getContactInformation());
        }
        String partyRole = party.getFlags() != null ? party.getFlags().getRoleOnCase() : null;
        return MediationLitigant.builder()
            .partyID(party.getPartyID())
            .partyRole(partyRole)
            .partyType(party.getType())
            .partyName(party.getPartyName())
            .paperResponse(PAPER_RESPONSE)
            .represented(true)
            .solicitorOrgName(solicitorOrgDetails != null ? solicitorOrgDetails.getName() : null)
            .litigantEmail(solicitorEmail)
            .litigantTelephone(solicitorOrgDetails != null ? solicitorOrgDetails.getCompanyNumber() : null)
            .mediationContactName(null)
            .mediationContactNumber(null)
            .mediationContactEmail(null)
            .dateRangeToAvoid(List.of(UnavailableDate.builder().build()))
            .build();
    }

    private MediationLitigant buildUnrepresentedLitigant(Party party) {
        String partyRole = party.getFlags() != null ? party.getFlags().getRoleOnCase() : null;
        return MediationLitigant.builder()
            .partyID(party.getPartyID())
            .partyRole(partyRole)
            .partyType(party.getType())
            .partyName(party.getPartyName())
            .paperResponse(PAPER_RESPONSE)
            .represented(false)
            .solicitorOrgName(null)
            .litigantEmail(party.getPartyEmail())
            .litigantTelephone(party.getPartyPhone())
            .mediationContactName(null)
            .mediationContactNumber(null)
            .mediationContactEmail(null)
            .dateRangeToAvoid(List.of(UnavailableDate.builder().build()))
            .build();
    }
}
