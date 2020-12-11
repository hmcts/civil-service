package uk.gov.hmcts.reform.unspec.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.unspec.enums.dq.Language;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.common.Applicant;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.unspec.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.DocumentType;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.model.dq.HearingSupport;
import uk.gov.hmcts.reform.unspec.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.unspec.utils.MonetaryConversions;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N181;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor
public class DirectionsQuestionnaireGenerator implements TemplateDataGenerator<DirectionsQuestionnaireForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DirectionsQuestionnaireForm templateData = getTemplateData(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N181);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.DIRECTIONS_QUESTIONNAIRE)
        );
    }

    private String getFileName(CaseData caseData) {
        return String.format(N181.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData) {
        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData.getSolicitorReferences()))
            .submittedOn(caseData.getDefendantResponseDate())
            .applicant(getApplicant(caseData))
            .fileDirectionsQuestionnaire(respondent1DQ.getFileDirectionQuestionnaire())
            .disclosureOfElectronicDocuments(respondent1DQ.getDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(respondent1DQ.getDisclosureOfNonElectronicDocuments())
            .experts(getExperts(respondent1DQ))
            .witnesses(getWitnesses(respondent1DQ))
            .hearing(getHearing(respondent1DQ))
            .hearingSupport(getHearingSupport(respondent1DQ))
            .furtherInformation(respondent1DQ.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(respondent1DQ))
            .statementOfTruth(respondent1DQ.getStatementOfTruth())
            .allocatedTrack(caseData.getAllocatedTrack())
            .build();
    }

    private Applicant getApplicant(CaseData caseData) {
        Party applicant = caseData.getApplicant1();
        return Applicant.builder()
            .name(applicant.getPartyName())
            .primaryAddress(applicant.getPrimaryAddress())
            .build();
    }

    private Experts getExperts(Respondent1DQ respondent1DQ) {
        var experts = respondent1DQ.getExperts();
        return Experts.builder()
            .expertRequired(experts.getExpertRequired())
            .expertReportsSent(
                ofNullable(experts.getExpertReportsSent())
                    .map(ExpertReportsSent::getDisplayedValue)
                    .orElse(""))
            .jointExpertSuitable(experts.getJointExpertSuitable())
            .details(getExpertsDetails(respondent1DQ))
            .build();
    }

    private List<Expert> getExpertsDetails(Respondent1DQ respondent1DQ) {
        return unwrapElements(respondent1DQ.getExperts().getDetails())
            .stream()
            .map(expert -> Expert.builder()
                .name(expert.getName())
                .fieldOfExpertise(expert.getFieldOfExpertise())
                .whyRequired(expert.getWhyRequired())
                .formattedCost(NumberFormat.getCurrencyInstance(Locale.UK)
                                   .format(MonetaryConversions.penniesToPounds(expert.getEstimatedCost())))
                .build())
            .collect(toList());
    }

    private Witnesses getWitnesses(Respondent1DQ respondent1DQ) {
        var witnesses = respondent1DQ.getWitnesses();
        return Witnesses.builder()
            .witnessesToAppear(witnesses.getWitnessesToAppear())
            .details(unwrapElements(witnesses.getDetails()))
            .build();
    }

    private Hearing getHearing(Respondent1DQ respondent1DQ) {
        var hearing = respondent1DQ.getHearing();
        return Hearing.builder()
            .hearingLength(getHearingLength(respondent1DQ))
            .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
            .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
            .build();
    }

    private String getHearingLength(Respondent1DQ respondent1DQ) {
        var hearing = respondent1DQ.getHearing();
        switch (hearing.getHearingLength()) {
            case LESS_THAN_DAY:
                return hearing.getHearingLengthHours() + " hours";
            case ONE_DAY:
                return "One day";
            default:
                return hearing.getHearingLengthDays() + " days";
        }
    }

    private String getHearingSupport(Respondent1DQ respondent1DQ) {
        var stringBuilder = new StringBuilder();
        ofNullable(respondent1DQ.getHearingSupport())
            .map(HearingSupport::getRequirements)
            .orElse(List.of())
            .forEach(requirement -> {
                var hearingSupport = respondent1DQ.getHearingSupport();
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

    private WelshLanguageRequirements getWelshLanguageRequirements(Respondent1DQ respondent1DQ) {
        var welshLanguageRequirements = respondent1DQ.getWelshLanguageRequirements();
        return WelshLanguageRequirements.builder()
            .isPartyWelsh(welshLanguageRequirements.getIsPartyWelsh())
            .evidence(ofNullable(
                welshLanguageRequirements.getEvidence()).map(Language::getDisplayedValue).orElse(""))
            .court(ofNullable(
                welshLanguageRequirements.getCourt()).map(Language::getDisplayedValue).orElse(""))
            .documents(ofNullable(
                welshLanguageRequirements.getDocuments()).map(Language::getDisplayedValue).orElse(""))
            .build();
    }
}
