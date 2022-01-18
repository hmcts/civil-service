package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor
public class DirectionsQuestionnaireGenerator implements TemplateDataGenerator<DirectionsQuestionnaireForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final StateFlowEngine stateFlowEngine;
    private final RepresentativeService representativeService;
    private String currentDefendantFor1v2SingleSolIndividualResponse = null;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DirectionsQuestionnaireForm templateData = getTemplateData(caseData);

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N181);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.DIRECTIONS_QUESTIONNAIRE)
        );
    }

    public CaseDocument generateDQFor1v2SingleSolDiffResponse(CaseData caseData,
                                                              String authorisation,
                                                              String respondent) {
        DirectionsQuestionnaireForm templateData = null;

        if (respondent.equals("ONE")) {
            currentDefendantFor1v2SingleSolIndividualResponse = "ONE";
            templateData = getRespondent1TemplateData(caseData);
        } else if (respondent.equals("TWO")) {
            currentDefendantFor1v2SingleSolIndividualResponse = "TWO";
            templateData = getRespondent2TemplateData(caseData);
        }

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N181);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData), docmosisDocument.getBytes(), DocumentType.DIRECTIONS_QUESTIONNAIRE)
        );
    }

    private String getFileName(CaseData caseData) {
        String userPrefix = isRespondentState(caseData) ? "defendant" : "claimant";
        return String.format(N181.getDocumentTitle(), userPrefix, caseData.getLegacyCaseReference());
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData) {
        DQ dq = isRespondentState(caseData) ? caseData.getRespondent1DQ() : caseData.getApplicant1DQ();

        if (isRespondent2(caseData)) {
            dq = caseData.getRespondent2DQ();
            return DirectionsQuestionnaireForm.builder()
                .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
                .referenceNumber(caseData.getLegacyCaseReference())
                .solicitorReferences(DocmosisTemplateDataUtils
                                         .fetchSolicitorReferences(caseData.getSolicitorReferences()))
                .submittedOn(caseData.getRespondent2ResponseDate().toLocalDate())
                .applicant(getApplicant(caseData))
                .respondents(getRespondents(caseData))
                .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
                .disclosureOfElectronicDocuments(dq.getDisclosureOfElectronicDocuments())
                .disclosureOfNonElectronicDocuments(dq.getDisclosureOfNonElectronicDocuments())
                .experts(getExperts(dq))
                .witnesses(getWitnesses(dq))
                .hearing(getHearing(dq))
                .hearingSupport(getHearingSupport(dq))
                .furtherInformation(dq.getFurtherInformation())
                .welshLanguageRequirements(getWelshLanguageRequirements(dq))
                .statementOfTruth(dq.getStatementOfTruth())
                .allocatedTrack(caseData.getAllocatedTrack())
                .build();
        }

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData.getSolicitorReferences()))
            .submittedOn(caseData.getRespondent1ResponseDate().toLocalDate())
            .applicant(getApplicant(caseData))
            .respondents(getRespondents(caseData))
            .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .disclosureOfElectronicDocuments(dq.getDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(dq.getDisclosureOfNonElectronicDocuments())
            .experts(getExperts(dq))
            .witnesses(getWitnesses(dq))
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .furtherInformation(dq.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .allocatedTrack(caseData.getAllocatedTrack())
            .build();
    }

    private DirectionsQuestionnaireForm getRespondent2TemplateData(CaseData caseData) {
        DQ dq = caseData.getRespondent2DQ();

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData.getSolicitorReferences()))
            .submittedOn(caseData.getRespondent2ResponseDate().toLocalDate())
            .applicant(getApplicant(caseData))
            .respondents(getRespondents(caseData))
            .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .disclosureOfElectronicDocuments(dq.getDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(dq.getDisclosureOfNonElectronicDocuments())
            .experts(getExperts(dq))
            .witnesses(getWitnesses(dq))
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .furtherInformation(dq.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .allocatedTrack(caseData.getAllocatedTrack())
            .build();
    }

    private DirectionsQuestionnaireForm getRespondent1TemplateData(CaseData caseData) {
        DQ dq = caseData.getRespondent1DQ();

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData.getSolicitorReferences()))
            .submittedOn(caseData.getRespondent1ResponseDate().toLocalDate())
            .applicant(getApplicant(caseData))
            .respondents(getRespondents(caseData))
            .fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .disclosureOfElectronicDocuments(dq.getDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(dq.getDisclosureOfNonElectronicDocuments())
            .experts(getExperts(dq))
            .witnesses(getWitnesses(dq))
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .furtherInformation(dq.getFurtherInformation())
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .allocatedTrack(caseData.getAllocatedTrack())
            .build();
    }

    private Boolean isRespondentState(CaseData caseData) {
        String state = stateFlowEngine.evaluate(caseData).getState().getName();
        return state.equals(FULL_DEFENCE.fullName())
            || state.equals(AWAITING_RESPONSES_RECEIVED.fullName())
            || state.equals(ALL_RESPONSES_RECEIVED.fullName());
    }

    private Party getApplicant(CaseData caseData) {
        var applicant = caseData.getApplicant1();
        return Party.builder()
            .name(applicant.getPartyName())
            .primaryAddress(applicant.getPrimaryAddress())
            .litigationFriendName(
                ofNullable(caseData.getApplicant1LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .build();
    }

    private boolean isRespondent2(CaseData caseData) {
        if (caseData.getRespondent2ResponseDate() != null && caseData.getRespondent1ResponseDate() == null) {
            return true;
        } else if ((caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent2ResponseDate() != null)) {
            if (caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate())) {
                return true;
            }
        }
        return false;
    }

    private List<Party> getRespondents(CaseData caseData) {
        var respondent = caseData.getRespondent1();
        var respondentRepresentative = representativeService.getRespondent1Representative(caseData);

        if (respondent2HasSameLegalRep(caseData)) {
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                return List.of(Party.builder()
                                   .name(caseData.getRespondent1().getPartyName())
                                   .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                                   .representative(representativeService.getRespondent1Representative(caseData))
                                   .litigationFriendName(
                                       ofNullable(caseData.getRespondent1LitigationFriend())
                                           .map(LitigationFriend::getFullName)
                                           .orElse(""))
                                   .build(),
                               Party.builder()
                                   .name(caseData.getRespondent2().getPartyName())
                                   .primaryAddress(caseData.getRespondent2().getPrimaryAddress())
                                   .representative(representativeService.getRespondent2Representative(caseData))
                                   .litigationFriendName(
                                       ofNullable(caseData.getRespondent1LitigationFriend())
                                           .map(LitigationFriend::getFullName)
                                           .orElse(""))
                                   .build());
            } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                if (currentDefendantFor1v2SingleSolIndividualResponse.equals("ONE")) {
                    // TODO remove the variable use as class variable and add specific test to check below party
                    return List.of(Party.builder()
                                       .name(caseData.getRespondent1().getPartyName())
                                       .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                                       .representative(representativeService.getRespondent1Representative(caseData))
                                       .litigationFriendName(
                                           ofNullable(caseData.getRespondent1LitigationFriend())
                                               .map(LitigationFriend::getFullName)
                                               .orElse(""))
                                       .build());
                } else if (currentDefendantFor1v2SingleSolIndividualResponse.equals("TWO")) {
                    return List.of(Party.builder()
                                       .name(caseData.getRespondent2().getPartyName())
                                       .primaryAddress(caseData.getRespondent2().getPrimaryAddress())
                                       .representative(representativeService.getRespondent2Representative(caseData))
                                       .litigationFriendName(
                                           ofNullable(caseData.getRespondent1LitigationFriend())
                                               .map(LitigationFriend::getFullName)
                                               .orElse(""))
                                       .build());
                }
            }
        }

        if (isRespondent2(caseData)) {
            respondent = caseData.getRespondent2();
            respondentRepresentative = representativeService.getRespondent2Representative(caseData);
        }

        return List.of(Party.builder()
                           .name(respondent.getPartyName())
                           .primaryAddress(respondent.getPrimaryAddress())
                           .representative(respondentRepresentative)
                           .litigationFriendName(
                               ofNullable(caseData.getRespondent1LitigationFriend())
                                   .map(LitigationFriend::getFullName)
                                   .orElse(""))
                           .build());
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private Experts getExperts(DQ dq) {
        var experts = dq.getExperts();
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

    private List<Expert> getExpertsDetails(DQ dq) {
        return unwrapElements(dq.getExperts().getDetails())
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

    private Witnesses getWitnesses(DQ dq) {
        var witnesses = dq.getWitnesses();
        return Witnesses.builder()
            .witnessesToAppear(witnesses.getWitnessesToAppear())
            .details(unwrapElements(witnesses.getDetails()))
            .build();
    }

    private Hearing getHearing(DQ dq) {
        var hearing = dq.getHearing();
        return Hearing.builder()
            .hearingLength(getHearingLength(dq))
            .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
            .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
            .build();
    }

    private String getHearingLength(DQ dq) {
        var hearing = dq.getHearing();
        switch (hearing.getHearingLength()) {
            case LESS_THAN_DAY:
                return hearing.getHearingLengthHours() + " hours";
            case ONE_DAY:
                return "One day";
            default:
                return hearing.getHearingLengthDays() + " days";
        }
    }

    private String getHearingSupport(DQ dq) {
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

    private WelshLanguageRequirements getWelshLanguageRequirements(DQ dq) {
        var welshLanguageRequirements = dq.getWelshLanguageRequirements();
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
