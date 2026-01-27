package uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor
public class RespondentTemplateForDQGenerator {

    private final SetApplicantsForDQGenerator setApplicantsForDQGenerator;
    private final GetRespondentsForDQGenerator respondentsForDQGenerator;
    private final FeatureToggleService featureToggleService;
    private final LocationReferenceDataService locationRefDataService;
    static final String SMALL_CLAIM = "SMALL_CLAIM";

    public DirectionsQuestionnaireForm getRespondent2TemplateData(CaseData caseData, String defendantIdentifier, String authorisation) {
        DQ dq = caseData.getRespondent2DQ();
        List<Party> respondents = respondentsForDQGenerator.getRespondents(caseData, defendantIdentifier);

        return new DirectionsQuestionnaireForm()
            .setCaseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .setReferenceNumber(caseData.getLegacyCaseReference())
            .setSolicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .setSubmittedOn(caseData.getRespondent2SameLegalRepresentative().equals(YES)
                             ? caseData.getRespondent1ResponseDate().toLocalDate()
                             : caseData.getRespondent2ResponseDate().toLocalDate())
            .setApplicant(setApplicantsForDQGenerator.getApplicant1DQParty(caseData))
            .setRespondents(respondents)
            .setRepresentativeOrganisationName(getOrgNameFromParties(respondents))
            .setFileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .setFixedRecoverableCosts(FixedRecoverableCostsSection.from(INTERMEDIATE_CLAIM.toString().equals(getClaimTrack(caseData))
                                                                         ? dq.getFixedRecoverableCostsIntermediate()
                                                                         : dq.getFixedRecoverableCosts()))
            .setDisclosureOfElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                 ? dq.getDisclosureOfElectronicDocuments() : dq.getSpecDisclosureOfElectronicDocuments())
            .setDisclosureOfNonElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                    ? dq.getDisclosureOfNonElectronicDocuments() : dq.getSpecDisclosureOfNonElectronicDocuments())
            .setDisclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .setDeterWithoutHearingYesNo(getDeterWithoutHearing(caseData, dq))
            .setDeterWithoutHearingWhyNot(getDeterWithoutHearing(caseData, dq) != null && dq.getDeterWithoutHearing().getDeterWithoutHearingYesNo().equals(NO)
                                           ? dq.getDeterWithoutHearing().getDeterWithoutHearingWhyNot() : null)
            .setExperts(SMALL_CLAIM.equals(caseData.getResponseClaimTrack())
                         ? getSmallClaimExperts(dq, caseData, defendantIdentifier) : getExperts(dq))
            .setWitnesses(getWitnesses(dq))
            .setHearing(getHearing(dq))
            .setHearingSupport(getHearingSupport(dq))
            .setSupport(dq.getHearingSupport())
            .setFurtherInformation(dq.getFurtherInformation())
            .setWelshLanguageRequirements(getWelshLanguageRequirements(dq))
            .setStatementOfTruth(dq.getStatementOfTruth())
            .setStatementOfTruthText(getStatementOfTruthText())
            .setVulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .setAllocatedTrack(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                ? caseData.getAllocatedTrack().name() : caseData.getResponseClaimTrack())
            .setRequestedCourt(getRequestedCourt(dq, authorisation));
    }

    public DirectionsQuestionnaireForm getRespondent1TemplateData(CaseData caseData, String defendantIdentifier, String authorisation) {
        DQ dq = caseData.getRespondent1DQ();

        List<Party> respondents = respondentsForDQGenerator.getRespondents(caseData, defendantIdentifier);

        return new DirectionsQuestionnaireForm()
            .setCaseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .setReferenceNumber(caseData.getLegacyCaseReference())
            .setSolicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .setSubmittedOn(caseData.getRespondent1ResponseDate().toLocalDate())
            .setApplicant(setApplicantsForDQGenerator.getApplicant1DQParty(caseData))
            .setRepresentativeOrganisationName(getOrgNameFromParties(respondents))
            .setFileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .setFixedRecoverableCosts(FixedRecoverableCostsSection.from(INTERMEDIATE_CLAIM.toString().equals(getClaimTrack(caseData))
                                                                         ? dq.getFixedRecoverableCostsIntermediate()
                                                                         : dq.getFixedRecoverableCosts()))
            .setDisclosureOfElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                 ? dq.getDisclosureOfElectronicDocuments() : dq.getSpecDisclosureOfElectronicDocuments())
            .setDisclosureOfNonElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                    ? dq.getDisclosureOfNonElectronicDocuments() : dq.getSpecDisclosureOfNonElectronicDocuments())
            .setDisclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .setDeterWithoutHearingYesNo(getDeterWithoutHearing(caseData, dq))
            .setDeterWithoutHearingWhyNot(getDeterWithoutHearing(caseData, dq) != null && dq.getDeterWithoutHearing().getDeterWithoutHearingYesNo().equals(NO)
                                           ? dq.getDeterWithoutHearing().getDeterWithoutHearingWhyNot() : null)
            .setExperts(SMALL_CLAIM.equals(caseData.getResponseClaimTrack())
                         ? getSmallClaimExperts(dq, caseData, defendantIdentifier) : getExperts(dq))
            .setWitnesses(getWitnesses(dq))
            .setHearing(getHearing(dq))
            .setHearingSupport(getHearingSupport(dq))
            .setSupport(dq.getHearingSupport())
            .setFurtherInformation(dq.getFurtherInformation())
            .setWelshLanguageRequirements(getWelshLanguageRequirements(dq))
            .setStatementOfTruth(dq.getStatementOfTruth())
            .setStatementOfTruthText(getStatementOfTruthText())
            .setVulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .setAllocatedTrack(getClaimTrack(caseData))
            .setRequestedCourt(getRequestedCourt(dq, authorisation));
    }

    private String getStatementOfTruthText() {
        String statementOfTruth = "The defendant believes that the facts stated in the response are true.";
        statementOfTruth += String.format(
            "\n\n\nI am duly authorised by the defendant to sign this statement.\n\n"
                + "The defendant understands that the proceedings for contempt of court "
                + "may be brought against anyone who makes, or causes to be made, "
                + "a false statement in a document verified by a statement of truth "
                + "without an honest belief in its truth."
        );
        return statementOfTruth;
    }

    private String getOrgNameFromParties(List<Party> parties) {
        return Optional.ofNullable(parties)
            .filter(p -> !p.isEmpty())
            .map(p -> p.get(0))
            .map(Party::getRepresentative)
            .map(Representative::getOrganisationName)
            .filter(StringUtils::isNotBlank)
            .orElse("");
    }

    private static YesOrNo getDeterWithoutHearing(CaseData caseData, DQ dq) {
        if (isSmallClaim(caseData) && nonNull(dq.getDeterWithoutHearing())) {
            return dq.getDeterWithoutHearing().getDeterWithoutHearingYesNo();
        }
        return null;
    }

    private static boolean isSmallClaim(CaseData caseData) {
        return AllocatedTrack.SMALL_CLAIM.equals(caseData.getAllocatedTrack())
            || SMALL_CLAIM.equals(caseData.getResponseClaimTrack());
    }

    public Experts getExperts(DQ dq) {
        var experts = dq.getExperts();
        if (experts == null) {
            return new Experts()
                .setExpertRequired(NO)
                .setDetails(Collections.emptyList());
        }
        return new Experts()
            .setExpertRequired(experts.getExpertRequired())
            .setExpertReportsSent(
                ofNullable(experts.getExpertReportsSent())
                    .map(ExpertReportsSent::getDisplayedValue)
                    .orElse(""))
            .setJointExpertSuitable(experts.getJointExpertSuitable())
            .setDetails(getExpertsDetails(dq));
    }

    public List<Expert> getExpertsDetails(DQ dq) {
        if (dq.getExperts().getDetails() == null) {
            return Collections.emptyList();
        }
        return unwrapElements(dq.getExperts().getDetails())
            .stream()
            .map(expert -> new Expert()
                .setFirstName(expert.getFirstName())
                .setLastName(expert.getLastName())
                .setPhoneNumber(expert.getPhoneNumber())
                .setEmailAddress(expert.getEmailAddress())
                .setFieldOfExpertise(expert.getFieldOfExpertise())
                .setWhyRequired(expert.getWhyRequired())
                .setFormattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                   .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost() == null ? new BigDecimal(0) : expert.getEstimatedCost()))))
            .collect(toList());
    }

    public Witnesses getWitnesses(DQ dq) {
        var witnesses = dq.getWitnesses();
        if (witnesses == null) {
            return new Witnesses()
                .setWitnessesToAppear(NO)
                .setDetails(Collections.emptyList());
        }
        List<Witness> witnessesList = ofNullable(witnesses.getDetails())
            .map(ElementUtils::unwrapElements)
            .orElseGet(Collections::emptyList);
        return new Witnesses()
            .setWitnessesToAppear(witnesses.getWitnessesToAppear())
            .setDetails(witnessesList);
    }

    private String getClaimTrack(CaseData caseData) {
        return UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? caseData.getAllocatedTrack().name() : caseData.getResponseClaimTrack();
    }

    private boolean shouldDisplayDisclosureReport(CaseData caseData) {
        // This is to hide disclosure report from prod
        if (MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            return featureToggleService.isMultiOrIntermediateTrackEnabled(caseData);
        } else if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && FAST_CLAIM.equals(caseData.getAllocatedTrack())) {
            return false;
        }
        return true;
    }

    public RequestedCourt getRequestedCourt(DQ dq, String authorisation) {
        RequestedCourt rc = dq.getRequestedCourt();
        if (rc != null && null !=  rc.getCaseLocation()) {
            List<LocationRefData> courtLocations = (locationRefDataService
                .getCourtLocationsByEpimmsIdAndCourtType(authorisation,
                                                         rc.getCaseLocation().getBaseLocation()
                ));
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setRequestHearingAtSpecificCourt(YES);
            requestedCourt.setReasonForHearingAtSpecificCourt(rc.getReasonForHearingAtSpecificCourt());
            courtLocations.stream()
                .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                .findFirst().ifPresent(court -> {
                    requestedCourt.setResponseCourtCode(court.getCourtLocationCode());
                    requestedCourt.setResponseCourtName(court.getCourtName());
                });
            return requestedCourt;
        } else {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setRequestHearingAtSpecificCourt(NO);
            return requestedCourt;
        }
    }

    public String getHearingLength(DQ dq) {
        var hearing = dq.getHearing();
        if (hearing == null || hearing.getHearingLength() == null) {
            return null;
        }
        switch (hearing.getHearingLength()) {
            case LESS_THAN_DAY:
                return hearing.getHearingLengthHours() + " hours";
            case ONE_DAY:
                return "One day";
            default:
                return hearing.getHearingLengthDays() + " days";
        }
    }

    private Hearing getHearing(DQ dq) {
        var hearing = dq.getHearing();
        if (hearing != null) {
            return new Hearing()
                .setHearingLength(getHearingLength(dq))
                .setUnavailableDatesRequired(hearing.getUnavailableDatesRequired())
                .setUnavailableDates(unwrapElements(hearing.getUnavailableDates()));
        } else {
            return null;
        }
    }

    public Experts getSmallClaimExperts(DQ dq, CaseData caseData, String defendantIdentifier) {
        var experts = dq.getSmallClaimExperts();
        YesOrNo expertRequired = defendantIdentifier == null || defendantIdentifier.equals("ONE")
            ? caseData.getResponseClaimExpertSpecRequired()
            : caseData.getResponseClaimExpertSpecRequired2();
        if (isClaimantResponse(caseData)) {
            expertRequired = caseData.getApplicantMPClaimExpertSpecRequired() != null
                ? caseData.getApplicantMPClaimExpertSpecRequired() : caseData.getApplicant1ClaimExpertSpecRequired();
        }
        Expert expertDetails;
        if (experts != null) {
            expertDetails = new Expert()
                .setFirstName(experts.getFirstName())
                .setLastName(experts.getLastName())
                .setPhoneNumber(experts.getPhoneNumber())
                .setEmailAddress(experts.getEmailAddress())
                .setFormattedCost(MonetaryConversions.penniesToPounds(experts.getEstimatedCost()).toString())
                .setFieldOfExpertise(experts.getFieldofExpertise())
                .setWhyRequired(experts.getWhyRequired());
        } else {
            expertDetails = new Expert();
        }

        return new Experts()
            .setExpertRequired(caseData.isRespondent1NotRepresented() ? YesOrNo.NO : expertRequired)
            .setExpertReportsSent(null)
            .setJointExpertSuitable(null)
            .setDetails(caseData.isRespondent1NotRepresented() && dq.getExperts() != null ? getExpertsDetails(dq) : List.of(expertDetails));
    }

    public static boolean isClaimantResponse(CaseData caseData) {
        var businessProcess = ofNullable(caseData.getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent)
            .orElse(null);
        return "CLAIMANT_RESPONSE".equals(businessProcess)
            || "CLAIMANT_RESPONSE_SPEC".equals(businessProcess)
            || "CLAIMANT_RESPONSE_CUI".equals(businessProcess);
    }

    public String getHearingSupport(DQ dq) {
        var stringBuilder = new StringBuilder();
        ofNullable(dq.getHearingSupport())
            .map(HearingSupport::getRequirements)
            .orElse(List.of())
            .forEach(requirement -> {
                var hearingSupport = dq.getHearingSupport();
                stringBuilder.append(requirement.getDisplayedValue());
                switch (requirement) {
                    case SIGN_INTERPRETER:
                        stringBuilder.append(" - ").append(hearingSupport.getSignLanguageRequired());
                        break;
                    case LANGUAGE_INTERPRETER:
                        stringBuilder.append(" - ").append(hearingSupport.getLanguageToBeInterpreted());
                        break;
                    case OTHER_SUPPORT:
                        stringBuilder.append(" - ").append(hearingSupport.getOtherSupport());
                        break;
                    default:
                        break;
                }
                stringBuilder.append("\n");
            });
        return stringBuilder.toString().trim();
    }

    public WelshLanguageRequirements getWelshLanguageRequirements(DQ dq) {
        var welshLanguageRequirements = dq.getWelshLanguageRequirements();
        if (welshLanguageRequirements == null) {
            return new WelshLanguageRequirements()
                .setEvidence("")
                .setCourt("")
                .setDocuments("");
        }
        return new WelshLanguageRequirements()
            .setEvidence(ofNullable(
                welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
            .setCourt(ofNullable(
                welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
            .setDocuments(ofNullable(
                welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""));
    }
}
