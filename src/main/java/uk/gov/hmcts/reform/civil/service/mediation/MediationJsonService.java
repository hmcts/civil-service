package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.VulnerabilityQuestions;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveCaseLevelFlags;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@AllArgsConstructor
public class MediationJsonService {

    private final OrganisationService organisationService;

    private static final String PAPER_RESPONSE = "N";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

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
        return buildRepresentedLitigant(caseData.getApplicant2(),
                                        caseData.getApp1MediationContactInfo(), caseData.getApp1MediationAvailability(),
                                        caseData.getApplicant2OrganisationPolicy() != null
                                            ? caseData.getApplicant2OrganisationPolicy()
                                            : caseData.getApplicant1OrganisationPolicy(),
                                        caseData.getApplicantSolicitor1UserDetails() != null
                                 ? caseData.getApplicantSolicitor1UserDetails().getEmail() : null);
    }

    private MediationLitigant buildRespondent1Fields(CaseData caseData) {
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
        List<MediationUnavailability> dateRangeToAvoid = getDateRangeToAvoid(mediationLiPCarm);

        String mediationContactName = YES.equals(mediationLiPCarm.getIsMediationContactNameCorrect())
            ? originalMediationContactPerson : mediationLiPCarm.getAlternativeMediationContactPerson();

        String mediationEmail = YES.equals(mediationLiPCarm.getIsMediationEmailCorrect())
            ? party.getPartyEmail() : mediationLiPCarm.getAlternativeMediationEmail();

        String mediationPhone = YES.equals(mediationLiPCarm.getIsMediationPhoneCorrect())
            ? party.getPartyPhone() : mediationLiPCarm.getAlternativeMediationTelephone();

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
            .mediationContactName(mediationContactName)
            .mediationContactNumber(mediationPhone)
            .mediationContactEmail(mediationEmail)
            .dateRangeToAvoid(dateRangeToAvoid)
            .build();
    }

    private MediationLitigant buildRepresentedLitigant(Party party,
                                                       MediationContactInformation mediationContactInformation,
                                                       MediationAvailability mediationAvailability,
                                                       OrganisationPolicy organisationPolicy, String solicitorEmail) {

        String solicitorOrgName = getSolicitorOrgName(organisationPolicy);
        String partyRole = party.getFlags() != null ? party.getFlags().getRoleOnCase() : null;
        String mediationContactName = getMediationContactName(mediationContactInformation);
        String mediationContactNumber = mediationContactInformation != null
            ? mediationContactInformation.getTelephoneNumber() : null;
        String mediationEmail = mediationContactInformation != null
            ? mediationContactInformation.getEmailAddress() : null;

        List<MediationUnavailability> dateRangeToAvoid = getDateRangeToAvoid(mediationAvailability);

        return MediationLitigant.builder()
            .partyID(party.getPartyID())
            .partyRole(partyRole)
            .partyType(party.getType())
            .partyName(party.getPartyName())
            .paperResponse(PAPER_RESPONSE)
            .represented(true)
            .solicitorOrgName(solicitorOrgName)
            .litigantEmail(solicitorEmail)
            .litigantTelephone(null)
            .mediationContactName(mediationContactName)
            .mediationContactNumber(mediationContactNumber)
            .mediationContactEmail(mediationEmail)
            .dateRangeToAvoid(dateRangeToAvoid)
            .build();
    }

    private void toMediationUnavailableDates(List<MediationUnavailability> toMediationUnavailability,
                                             List<Element<UnavailableDate>> unavailableDatesForMediation) {
        List<UnavailableDate> unavailableDates = unwrapElements(unavailableDatesForMediation);
        for (UnavailableDate unavailableDate : unavailableDates) {
            if (SINGLE_DATE.equals(unavailableDate.getUnavailableDateType())) {
                toMediationUnavailability.add(MediationUnavailability.builder()
                                                  .fromDate(formatDate(unavailableDate.getDate()))
                                                  .toDate(formatDate(unavailableDate.getDate()))
                                                  .build());
            }
            if (DATE_RANGE.equals(unavailableDate.getUnavailableDateType())) {
                toMediationUnavailability.add(MediationUnavailability.builder()
                                                  .fromDate(formatDate(unavailableDate.getFromDate()))
                                                  .toDate(formatDate(unavailableDate.getToDate()))
                                                  .build());
            }
        }
    }

    private List<MediationUnavailability> getDateRangeToAvoid(MediationAvailability mediationAvailability) {
        if (mediationAvailability != null) {
            if (YES.equals(mediationAvailability.getIsMediationUnavailablityExists())) {
                List<MediationUnavailability> toMediationUnavailability = new ArrayList<>();
                toMediationUnavailableDates(
                    toMediationUnavailability,
                    mediationAvailability.getUnavailableDatesForMediation());
                return toMediationUnavailability;
            }
        }
        return List.of(MediationUnavailability.builder().build());
    }

    private List<MediationUnavailability> getDateRangeToAvoid(MediationLiPCarm mediationLiPCarm) {
        List<MediationUnavailability> toMediationUnavailability = new ArrayList<>();
        if (YES.equals(mediationLiPCarm.getHasUnavailabilityNextThreeMonths())) {
            toMediationUnavailableDates(toMediationUnavailability, mediationLiPCarm.getUnavailableDatesForMediation());
        }
        return toMediationUnavailability;
    }

    private String formatDate(LocalDate unavailableDate) {
        return unavailableDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK));
    }

    @Nullable
    private String getMediationContactName(MediationContactInformation mediationContactInformation) {
        return mediationContactInformation != null
            ? String.format("%s %s", mediationContactInformation.getFirstName(), mediationContactInformation.getLastName())
            : null;
    }

    @Nullable
    private String getSolicitorOrgName(OrganisationPolicy organisationPolicy) {
        String orgId = organisationPolicy.getOrganisation().getOrganisationID();
        Optional<Organisation> organisation = organisationService.findOrganisationById(orgId);
        Organisation solicitorOrgDetails = organisation.orElse(null);
        return solicitorOrgDetails != null ? solicitorOrgDetails.getName() : null;
    }
}
