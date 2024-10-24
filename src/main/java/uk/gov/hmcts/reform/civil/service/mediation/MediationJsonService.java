package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.PartyFlags;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.VulnerabilityQuestions;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.service.mediation.helpers.PartyDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.mediation.helpers.RepresentedLitigantPopulator;
import uk.gov.hmcts.reform.civil.service.mediation.helpers.UnrepresentedLitigantPopulator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveCaseLevelFlags;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveFlags;

@Slf4j
@Service
@AllArgsConstructor
public class MediationJsonService {

    private final PartyDetailsPopulator partyDetailsPopulator;
    private final RepresentedLitigantPopulator representedLitigantPopulator;
    private final UnrepresentedLitigantPopulator unrepresentedLitigantPopulator;

    public MediationCase generateJsonContent(CaseData caseData) {
        log.info("Generate JSON content for case ID {}", caseData.getCcdCaseReference());
        List<MediationLitigant> litigantList = new ArrayList<>();

        boolean activeCaseFlags = buildCaseFlags(caseData);

        buildLitigantFields(caseData, litigantList);

        return MediationCase.builder()
            .ccdCaseNumber(caseData.getCcdCaseReference())
            .casemanCaseNumber(caseData.getLegacyCaseReference())
            .caseTitle(caseData.getCaseNameHmctsInternal())
            .caseFlags(activeCaseFlags)
            .claimValue(caseData.getTotalClaimAmount())
            .litigants(litigantList)
            .build();
    }

    private boolean buildCaseFlags(CaseData caseData) {
        log.info("Build case flags for case ID {}", caseData.getCcdCaseReference());
        return Stream.of(
            checkForActiveCaseFlags(caseData),
            checkApplicant1DQRequirements(caseData),
            checkRespondent1DQRequirements(caseData),
            checkRespondent2DQRequirements(caseData),
            checkApplicant2DQRequirements(caseData),
            checkLiPApplicantIsWelsh(caseData),
            checkLiPDefendantIsWelsh(caseData)
        ).anyMatch(Boolean::booleanValue);
    }

    private boolean checkLiPApplicantIsWelsh(CaseData caseData) {
        log.info("Check LiP Application is Welsh for case ID {}", caseData.getCcdCaseReference());
        return caseData.isClaimantBilingual();
    }

    private boolean checkLiPDefendantIsWelsh(CaseData caseData) {
        log.info("Check LiP Defendant is Welsh for case ID {}", caseData.getCcdCaseReference());
        return caseData.isRespondentResponseBilingual();
    }

    private boolean checkApplicant1DQRequirements(CaseData caseData) {
        log.info("Check Applicant 1 DQ requirements for case ID {}", caseData.getCcdCaseReference());
        if (caseData.getApplicant1DQ() == null) {
            return false;
        }

        return Stream.of(
            welshOrBilingualLanguage(caseData.getApplicant1DQ().getWelshLanguageRequirements()),
            vulnerabilityAdjustmentRequired(caseData.getApplicant1DQ().getVulnerabilityQuestions()),
            supportWithAccessNeedsRequired(caseData.getApplicant1DQ().getApplicant1DQHearingSupport())
        ).anyMatch(Boolean::booleanValue);
    }

    private boolean checkApplicant2DQRequirements(CaseData caseData) {
        log.info("Check Applicant 2 DQ requirements for case ID {}", caseData.getCcdCaseReference());
        if (caseData.getApplicant2DQ() == null) {
            return false;
        }

        return Stream.of(
            welshOrBilingualLanguage(caseData.getApplicant2DQ().getWelshLanguageRequirements()),
            vulnerabilityAdjustmentRequired(caseData.getApplicant2DQ().getVulnerabilityQuestions()),
            supportWithAccessNeedsRequired(caseData.getApplicant2DQ().getApplicant2DQHearingSupport())
        ).anyMatch(Boolean::booleanValue);
    }

    private boolean checkRespondent1DQRequirements(CaseData caseData) {
        log.info("Check Respondent 1 DQ requirements for case ID {}", caseData.getCcdCaseReference());
        if (caseData.getRespondent1DQ() == null) {
            return false;
        }

        return Stream.of(
            welshOrBilingualLanguage(caseData.getRespondent1DQ().getWelshLanguageRequirements()),
            vulnerabilityAdjustmentRequired(caseData.getRespondent1DQ().getVulnerabilityQuestions()),
            supportWithAccessNeedsRequired(caseData.getRespondent1DQ().getRespondent1DQHearingSupport())
        ).anyMatch(Boolean::booleanValue);
    }

    private boolean checkRespondent2DQRequirements(CaseData caseData) {
        log.info("Check Respondent 2 DQ requirements for case ID {}", caseData.getCcdCaseReference());
        if (caseData.getRespondent2DQ() == null) {
            return false;
        }

        return Stream.of(
            welshOrBilingualLanguage(caseData.getRespondent2DQ().getWelshLanguageRequirements()),
            vulnerabilityAdjustmentRequired(caseData.getRespondent2DQ().getVulnerabilityQuestions()),
            supportWithAccessNeedsRequired(caseData.getRespondent2DQ().getRespondent2DQHearingSupport())
        ).anyMatch(Boolean::booleanValue);
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

    private boolean checkForActiveCaseFlags(CaseData caseData) {
        log.info("Check for active case flags for case ID {}", caseData.getCcdCaseReference());
        List<FlagDetail> allActiveCaseLevelFlags = getAllActiveCaseLevelFlags(caseData);
        List<PartyFlags> allActivePartyLevelFlags = getAllActiveFlags(caseData);

        return !allActiveCaseLevelFlags.isEmpty() || !allActivePartyLevelFlags.isEmpty();
    }

    private void buildLitigantFields(CaseData caseData, List<MediationLitigant> litigantList) {
        log.info("Build litigant fields for case ID {}", caseData.getCcdCaseReference());
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
        log.info("Build Applicant 1 fields for case ID {}", caseData.getCcdCaseReference());
        if (NO.equals(caseData.getApplicant1Represented())) {
            return buildUnrepresentedLitigant(caseData.getApplicant1(),
                                              caseData.getCaseDataLiP().getApplicant1AdditionalLipPartyDetails() != null
                                                  ? caseData.getCaseDataLiP().getApplicant1AdditionalLipPartyDetails().getContactPerson() : null,
                                              caseData.getCaseDataLiP().getApplicant1LiPResponseCarm());
        } else {
            return buildRepresentedLitigant(caseData.getApplicant1(),
                                            caseData.getApp1MediationContactInfo(),
                                            caseData.getApp1MediationAvailability(),
                                            caseData.getApplicant1OrganisationPolicy(),
                                            caseData.getApplicantSolicitor1UserDetails() != null
                                     ? caseData.getApplicantSolicitor1UserDetails().getEmail() : null);
        }
    }

    private MediationLitigant buildApplicant2Fields(CaseData caseData) {
        log.info("Build Applicant 2 fields for case ID {}", caseData.getCcdCaseReference());
        return buildRepresentedLitigant(caseData.getApplicant2(),
                                        caseData.getApp1MediationContactInfo(), caseData.getApp1MediationAvailability(),
                                        caseData.getApplicant2OrganisationPolicy() != null
                                            ? caseData.getApplicant2OrganisationPolicy()
                                            : caseData.getApplicant1OrganisationPolicy(),
                                        caseData.getApplicantSolicitor1UserDetails() != null
                                 ? caseData.getApplicantSolicitor1UserDetails().getEmail() : null);
    }

    private MediationLitigant buildRespondent1Fields(CaseData caseData) {
        log.info("Build Respondent 1 fields for case ID {}", caseData.getCcdCaseReference());
        if (YES.equals(caseData.getRespondent1Represented())) {
            return buildRepresentedLitigant(caseData.getRespondent1(),
                                            caseData.getResp1MediationContactInfo(), caseData.getResp1MediationAvailability(),
                                            caseData.getRespondent1OrganisationPolicy(),
                                            caseData.getRespondentSolicitor1EmailAddress());
        } else {
            return buildUnrepresentedLitigant(caseData.getRespondent1(),
                                              caseData.getCaseDataLiP().getRespondent1LiPResponse() != null
                                                  ? caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1LiPContactPerson() : null,
                                              caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm());
        }
    }

    private MediationLitigant buildRespondent2Fields(CaseData caseData) {
        log.info("Build Respondent 2 fields for case ID {}", caseData.getCcdCaseReference());
        if (YES.equals(caseData.getRespondent2SameLegalRepresentative())) {
            return buildRepresentedLitigant(caseData.getRespondent2(),
                                            caseData.getResp1MediationContactInfo(), caseData.getResp1MediationAvailability(),
                                            caseData.getRespondent1OrganisationPolicy(),
                                            caseData.getRespondentSolicitor1EmailAddress());
        }
        return buildRepresentedLitigant(caseData.getRespondent2(),
                                        caseData.getResp2MediationContactInfo(), caseData.getResp2MediationAvailability(),
                                        caseData.getRespondent2OrganisationPolicy(),
                                        caseData.getRespondentSolicitor2EmailAddress());
    }

    private MediationLitigant buildUnrepresentedLitigant(Party party, String originalMediationContactPerson,
                                                         MediationLiPCarm mediationLiPCarm) {
        var unrepresentedLitigantBuilder = MediationLitigant.builder();
        partyDetailsPopulator.populator(unrepresentedLitigantBuilder, party);
        unrepresentedLitigantPopulator.populator(unrepresentedLitigantBuilder, party, originalMediationContactPerson, mediationLiPCarm);
        return unrepresentedLitigantBuilder.build();
    }

    private MediationLitigant buildRepresentedLitigant(Party party,
                                                       MediationContactInformation mediationContactInformation,
                                                       MediationAvailability mediationAvailability,
                                                       OrganisationPolicy organisationPolicy, String solicitorEmail) {

        var representedLitigantBuilder = MediationLitigant.builder();
        partyDetailsPopulator.populator(representedLitigantBuilder, party);
        representedLitigantPopulator.populator(representedLitigantBuilder, mediationContactInformation, mediationAvailability, organisationPolicy, solicitorEmail);
        return representedLitigantBuilder.build();
    }

}
