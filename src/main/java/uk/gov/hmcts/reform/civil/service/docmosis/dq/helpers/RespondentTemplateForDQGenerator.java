package uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
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

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

        return  DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .submittedOn(caseData.getRespondent2SameLegalRepresentative().equals(YES)
                             ? caseData.getRespondent1ResponseDate().toLocalDate()
                             : caseData.getRespondent2ResponseDate().toLocalDate())
            .applicant(setApplicantsForDQGenerator.getApplicant1DQParty(caseData))
            .respondents(respondentsForDQGenerator.getRespondents(caseData, defendantIdentifier))
            .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .fixedRecoverableCosts(FixedRecoverableCostsSection.from(INTERMEDIATE_CLAIM.toString().equals(getClaimTrack(caseData))
                                                                         ? dq.getFixedRecoverableCostsIntermediate()
                                                                         : dq.getFixedRecoverableCosts()))
            .disclosureOfElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                 ? dq.getDisclosureOfElectronicDocuments() : dq.getSpecDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                    ? dq.getDisclosureOfNonElectronicDocuments() : dq.getSpecDisclosureOfNonElectronicDocuments())
            .disclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .deterWithoutHearingYesNo(getDeterWithoutHearing(caseData, dq)  ? YES : null)
            .deterWithoutHearingWhyNot(getDeterWithoutHearing(caseData, dq) && dq.getDeterWithoutHearing().getDeterWithoutHearingYesNo().equals(NO)
                                           ? dq.getDeterWithoutHearing().getDeterWithoutHearingWhyNot() : null)
            .experts(SMALL_CLAIM.equals(caseData.getResponseClaimTrack())
                         ? getSmallClaimExperts(dq, caseData, defendantIdentifier) : getExperts(dq))
            .witnesses(getWitnesses(dq))
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .support(dq.getHearingSupport())
            .furtherInformation(dq.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .allocatedTrack(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                ? caseData.getAllocatedTrack().name() : caseData.getResponseClaimTrack())
            .requestedCourt(getRequestedCourt(dq, authorisation))
            .build();
    }

    public DirectionsQuestionnaireForm getRespondent1TemplateData(CaseData caseData, String defendantIdentifier, String authorisation) {
        DQ dq = caseData.getRespondent1DQ();

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .submittedOn(caseData.getRespondent1ResponseDate().toLocalDate())
            .applicant(setApplicantsForDQGenerator.getApplicant1DQParty(caseData))
            .respondents(respondentsForDQGenerator.getRespondents(caseData, defendantIdentifier))
            .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .fixedRecoverableCosts(FixedRecoverableCostsSection.from(INTERMEDIATE_CLAIM.toString().equals(getClaimTrack(caseData))
                                                                         ? dq.getFixedRecoverableCostsIntermediate()
                                                                         : dq.getFixedRecoverableCosts()))
            .disclosureOfElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                 ? dq.getDisclosureOfElectronicDocuments() : dq.getSpecDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                    ? dq.getDisclosureOfNonElectronicDocuments() : dq.getSpecDisclosureOfNonElectronicDocuments())
            .disclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .deterWithoutHearingYesNo(getDeterWithoutHearing(caseData, dq)  ? YES : null)
            .deterWithoutHearingWhyNot(getDeterWithoutHearing(caseData, dq) && dq.getDeterWithoutHearing().getDeterWithoutHearingYesNo().equals(NO)
                                           ? dq.getDeterWithoutHearing().getDeterWithoutHearingWhyNot() : null)
            .experts(SMALL_CLAIM.equals(caseData.getResponseClaimTrack())
                         ? getSmallClaimExperts(dq, caseData, defendantIdentifier) : getExperts(dq))
            .witnesses(getWitnesses(dq))
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .support(dq.getHearingSupport())
            .furtherInformation(dq.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .allocatedTrack(getClaimTrack(caseData))
            .requestedCourt(getRequestedCourt(dq, authorisation))
            .build();
    }

    private static boolean getDeterWithoutHearing(CaseData caseData, DQ dq) {
        return isSmallClaim(caseData) && nonNull(dq.getDeterWithoutHearing());
    }

    private static boolean isSmallClaim(CaseData caseData) {
        return AllocatedTrack.SMALL_CLAIM.equals(caseData.getAllocatedTrack())
            || SMALL_CLAIM.equals(caseData.getResponseClaimTrack());
    }

    public Experts getExperts(DQ dq) {
        var experts = dq.getExperts();
        if (experts == null) {
            return Experts.builder().expertRequired(NO)
                .details(Collections.emptyList())
                .build();
        }
        return Experts.builder()
            .expertRequired(experts.getExpertRequired())
            .expertReportsSent(
                ofNullable(experts.getExpertReportsSent())
                    .map(ExpertReportsSent::getDisplayedValue)
                    .orElse(""))
            .jointExpertSuitable(experts.getJointExpertSuitable())
            .details(getExpertsDetails(dq))
            .build();
    }

    public List<Expert> getExpertsDetails(DQ dq) {
        if (dq.getExperts().getDetails() == null) {
            return Collections.emptyList();
        }
        return unwrapElements(dq.getExperts().getDetails())
            .stream()
            .map(expert -> Expert.builder()
                .firstName(expert.getFirstName())
                .lastName(expert.getLastName())
                .phoneNumber(expert.getPhoneNumber())
                .emailAddress(expert.getEmailAddress())
                .fieldOfExpertise(expert.getFieldOfExpertise())
                .whyRequired(expert.getWhyRequired())
                .formattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                   .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost())))
                .build())
            .collect(toList());
    }

    public Witnesses getWitnesses(DQ dq) {
        var witnesses = dq.getWitnesses();
        if (witnesses == null) {
            return Witnesses.builder().witnessesToAppear(NO)
                .details(Collections.emptyList())
                .build();
        }
        List<Witness> witnessesList = ofNullable(witnesses.getDetails())
            .map(ElementUtils::unwrapElements)
            .orElseGet(Collections::emptyList);
        return Witnesses.builder()
            .witnessesToAppear(witnesses.getWitnessesToAppear())
            .details(witnessesList)
            .build();
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
            RequestedCourt.RequestedCourtBuilder builder = RequestedCourt.builder()
                .requestHearingAtSpecificCourt(YES)
                .reasonForHearingAtSpecificCourt(rc.getReasonForHearingAtSpecificCourt());
            courtLocations.stream()
                .filter(id -> id.getCourtTypeId().equals(CIVIL_COURT_TYPE_ID))
                .findFirst().ifPresent(court -> builder
                    .responseCourtCode(court.getCourtLocationCode())
                    .responseCourtName(court.getCourtName()));
            return builder.build();
        } else {
            return RequestedCourt.builder()
                .requestHearingAtSpecificCourt(NO)
                .build();
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
            return Hearing.builder()
                .hearingLength(getHearingLength(dq))
                .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
                .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
                .build();
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
            expertDetails = Expert.builder()
                .firstName(experts.getFirstName())
                .lastName(experts.getLastName())
                .phoneNumber(experts.getPhoneNumber())
                .emailAddress(experts.getEmailAddress())
                .formattedCost(MonetaryConversions.penniesToPounds(experts.getEstimatedCost()).toString())
                .fieldOfExpertise(experts.getFieldofExpertise())
                .whyRequired(experts.getWhyRequired())
                .build();
        } else {
            expertDetails = Expert.builder().build();
        }

        return Experts.builder()
            .expertRequired(caseData.isRespondent1NotRepresented() ? YesOrNo.NO : expertRequired)
            .expertReportsSent(null)
            .jointExpertSuitable(null)
            .details(caseData.isRespondent1NotRepresented() && dq.getExperts() != null ? getExpertsDetails(dq) : List.of(expertDetails))
            .build();
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
            return WelshLanguageRequirements.builder()
                .evidence("")
                .court("")
                .documents("")
                .build();
        }
        return WelshLanguageRequirements.builder()
            .evidence(ofNullable(
                welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
            .court(ofNullable(
                welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
            .documents(ofNullable(
                welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""))
            .build();
    }
}
