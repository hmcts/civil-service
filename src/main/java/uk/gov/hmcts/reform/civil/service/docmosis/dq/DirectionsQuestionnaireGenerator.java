package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
import uk.gov.hmcts.reform.civil.model.dq.DisclosureReport;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.FutureApplications;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_2V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
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
        DocmosisTemplates templateId;
        DocmosisDocument docmosisDocument;
        DirectionsQuestionnaireForm templateData;
        if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            templateId = DocmosisTemplates.DEFENDANT_RESPONSE_SPEC;
        } else {
            templateId = getDocmosisTemplate(caseData);
        }

        templateData = getTemplateData(caseData);
        docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, templateId);

        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                    DocumentType.DIRECTIONS_QUESTIONNAIRE
            )
        );
    }

    private DocmosisTemplates getDocmosisTemplate(CaseData caseData) {
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_ONE_LEGAL_REP:
                if(!isClaimantResponse(caseData)){
                    return N181_MULTIPARTY_SAME_SOL;
                }
                //FALL-THROUGH
            case TWO_V_ONE:
                if(!isMultipartyClaimantResponseAndProceedingWithOnlyOneClaim(caseData)){
                    return N181_2V1;
                }
                //FALL-THROUGH
            default:
                return N181;
        }
    }

    public CaseDocument generateDQFor1v2SingleSolDiffResponse(CaseData caseData,
                                                              String authorisation,
                                                              String respondent) {
        DocmosisTemplates templateId = TWO_V_ONE.equals(MultiPartyScenario
                                                            .getMultiPartyScenario(caseData)) ? N181_2V1 : N181;
        DirectionsQuestionnaireForm templateData;

        if (respondent.equals("ONE")) {
            currentDefendantFor1v2SingleSolIndividualResponse = "ONE";
            templateData = getRespondent1TemplateData(caseData);
        } else if (respondent.equals("TWO")) {
            currentDefendantFor1v2SingleSolIndividualResponse = "TWO";
            templateData = getRespondent2TemplateData(caseData);
        } else {
            throw new IllegalArgumentException("Respondent argument is expected to be one of ONE or TWO");
        }

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N181);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                    DocumentType.DIRECTIONS_QUESTIONNAIRE
            )
        );
    }

    private String getFileName(CaseData caseData, DocmosisTemplates templateId) {
        String userPrefix = isRespondentState(caseData) ? "defendant" : "claimant";
        return String.format(templateId.getDocumentTitle(), userPrefix, caseData.getLegacyCaseReference());
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData) {
        DQ dq = isRespondentState(caseData) ? caseData.getRespondent1DQ() : caseData.getApplicant1DQ();
        if (onlyApplicant2IsProceeding(caseData)) {
            dq = caseData.getApplicant2DQ();
        }
        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils
                                     .fetchSolicitorReferences(caseData.getSolicitorReferences()))
            .applicant(getApplicant(caseData))
            .respondents(getRespondents(caseData))
            .allocatedTrack(caseData.getAllocatedTrack());

        if (isRespondent2(caseData) && !isClaimantResponse(caseData)) {
            dq = caseData.getRespondent2DQ();
            builder.submittedOn(caseData.getRespondent2ResponseDate().toLocalDate());
        } else {
            builder.submittedOn(caseData.getRespondent1ResponseDate().toLocalDate())
                .applicant2(TWO_V_ONE.equals(MultiPartyScenario
                                                 .getMultiPartyScenario(caseData)) ? getApplicant2(caseData) : null);
        }

        Witnesses witnesses = getWitnesses(dq);
        int witnessesIncludingDefendants = countWitnessesIncludingDefendant(witnesses, caseData);

        builder.fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .disclosureOfElectronicDocuments(dq.getDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(dq.getDisclosureOfNonElectronicDocuments())
            .experts(getExperts(dq))
            .witnesses(witnesses)
            .witnessesIncludingDefendants(witnessesIncludingDefendants)
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .furtherInformation(getFurtherInformation(dq, caseData))
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .disclosureReport(getDisclosureReport(dq))
            .requestedCourt(getRequestedCourt(dq));

        return builder.build();
    }

    private int countWitnessesIncludingDefendant(Witnesses witnesses, CaseData caseData) {
        int witnessesIncludingDefendants;
        if (AllocatedTrack.SMALL_CLAIM.equals(caseData.getAllocatedTrack())) {
            if (StringUtils.isNotBlank(caseData.getResponseClaimWitnesses())
                && caseData.getResponseClaimWitnesses().matches("\\d+")) {
                witnessesIncludingDefendants = Integer.parseInt(caseData.getResponseClaimWitnesses());
            } else {
                witnessesIncludingDefendants = 0;
            }
        } else {
            witnessesIncludingDefendants = YES.equals(witnesses.getWitnessesToAppear())
                ? witnesses.getDetails().size() : 0;
            MultiPartyScenario multiParty = MultiPartyScenario.getMultiPartyScenario(caseData);
            if (multiParty == MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP
                || multiParty == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
                witnessesIncludingDefendants += 2;
            } else {
                witnessesIncludingDefendants += 1;
            }
        }
        return witnessesIncludingDefendants;
    }

    private FurtherInformation getFurtherInformation(DQ dq, CaseData caseData) {
        Optional<FurtherInformation> dqFurtherInformation = ofNullable(dq.getFurtherInformation());
        Optional<FutureApplications> r1dqFutureApplications = ofNullable(
            dq instanceof Respondent1DQ ? (Respondent1DQ) dq : null
        ).map(Respondent1DQ::getFutureApplications);

        YesOrNo wantMore = Stream.of(
            r1dqFutureApplications
                .map(FutureApplications::getIntentionToMakeFutureApplications),
            dqFurtherInformation
                .map(FurtherInformation::getFutureApplications)
        ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(YesOrNo.NO);

        String whatMoreFor = NO.equals(wantMore) ? null :
            Stream.of(
                r1dqFutureApplications
                    .map(FutureApplications::getWhatWillFutureApplicationsBeMadeFor),
                dqFurtherInformation
                    .map(FurtherInformation::getReasonForFutureApplications)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);

        String furtherJudgeInfo = NO.equals(wantMore) ? null :
            Stream.of(
                Optional.ofNullable(caseData.getAdditionalInformationForJudge()),
                dqFurtherInformation
                    .map(FurtherInformation::getOtherInformationForJudge)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);

        return FurtherInformation.builder()
            .futureApplications(wantMore)
            .intentionToMakeFutureApplications(wantMore)
            .reasonForFutureApplications(whatMoreFor)
            .otherInformationForJudge(furtherJudgeInfo)
            .build();
    }

    private RequestedCourt getRequestedCourt(DQ dq) {
        RequestedCourt rc = dq.getRequestedCourt();
        if (rc == null) {
            return RequestedCourt.builder()
                .requestHearingAtSpecificCourt(NO)
                .build();
        } else {
            return RequestedCourt.builder()
                .requestHearingAtSpecificCourt(ofNullable(rc.getRequestHearingAtSpecificCourt()).orElse(NO))
                .responseCourtCode(rc.getResponseCourtCode())
                .reasonForHearingAtSpecificCourt(rc.getReasonForHearingAtSpecificCourt())
                .build();
        }
    }

    private DisclosureReport getDisclosureReport(DQ dq) {
        DisclosureReport dr = dq.getDisclosureReport();
        if (dr == null) {
            return DisclosureReport.builder().disclosureProposalAgreed(NO)
                .disclosureFormFiledAndServed(NO)
                .build();
        } else {
            return DisclosureReport.builder()
                .disclosureFormFiledAndServed(ofNullable(dr.getDisclosureFormFiledAndServed()).orElse(NO))
                .disclosureProposalAgreed(ofNullable(dr.getDisclosureProposalAgreed()).orElse(NO))
                .draftOrderNumber(dr.getDraftOrderNumber())
                .build();
        }
    }

    private boolean isClaimantResponse(CaseData caseData) {
        return caseData.getApplicant1ResponseDate() != null;
    }

    private boolean isMultipartyClaimantResponseAndProceedingWithOnlyOneClaim(CaseData caseData) {
        if (isClaimantResponse(caseData) == false) {
            return false;
        }

        switch (getMultiPartyScenario(caseData)) {
            case TWO_V_ONE:
                // XOR: If they are the opposite of each other - Divergent response
                if (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    ^ YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())) {
                    return true;
                }
                // FALL-THROUGH
            case ONE_V_TWO_ONE_LEGAL_REP:
            case ONE_V_TWO_TWO_LEGAL_REP:
                // XOR: If they are the opposite of each other - Divergent response
                if (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
                    ^ YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())) {
                    return false;
                }
                // FALL-THROUGH
            default: {
                return false;
            }
        }
    }

    private boolean onlyApplicant2IsProceeding(CaseData caseData) {
        return isMultipartyClaimantResponseAndProceedingWithOnlyOneClaim(caseData)
            && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
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
        if (isClaimantResponse(caseData)) {
            return false;
        }
        String state = stateFlowEngine.evaluate(caseData).getState().getName();

        return SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION
            || state.equals(FULL_DEFENCE.fullName())
            || state.equals(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())
            || state.equals(ALL_RESPONSES_RECEIVED.fullName())
            || state.equals(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName());
    }

    private Party getApplicant(CaseData caseData) {
        var applicant = caseData.getApplicant1();

        if (onlyApplicant2IsProceeding(caseData)) {
            applicant = caseData.getApplicant2();
        }

        return Party.builder()
            .name(applicant.getPartyName())
            .primaryAddress(applicant.getPrimaryAddress())
            .litigationFriendName(
                ofNullable(caseData.getApplicant1LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .build();
    }

    private Party getApplicant2(CaseData caseData) {
        if (onlyApplicant2IsProceeding(caseData)) {
            return null;
        }

        var applicant = caseData.getApplicant2();
        return Party.builder()
            .name(applicant.getPartyName())
            .primaryAddress(applicant.getPrimaryAddress())
            .litigationFriendName(
                ofNullable(caseData.getApplicant2LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .build();
    }

    private boolean isRespondent2(CaseData caseData) {
        if (caseData.getRespondent2ResponseDate() != null) {
            return caseData.getRespondent1ResponseDate() == null
                || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate());
        }

        return false;
    }

    private List<Party> getRespondents(CaseData caseData) {
        var respondent = caseData.getRespondent1();
        var respondentRepresentative = representativeService.getRespondent1Representative(caseData);

        if (isClaimantResponse(caseData) &&
            (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())
            || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()))) {

            List<Party> respondents = new ArrayList<>();
            if (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2())) {
                respondents.add(Party.builder()
                                    .name(caseData.getRespondent1().getPartyName())
                                    .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                                    .representative(representativeService
                                                        .getRespondent1Representative(caseData))
                                    .litigationFriendName(
                                        ofNullable(caseData.getRespondent1LitigationFriend())
                                            .map(LitigationFriend::getFullName)
                                            .orElse(""))
                                    .build());
            }
            if (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2())) {
                respondents.add(Party.builder()
                                    .name(caseData.getRespondent2().getPartyName())
                                    .primaryAddress(caseData.getRespondent2().getPrimaryAddress())
                                    .representative(representativeService
                                                        .getRespondent2Representative(caseData))
                                    .litigationFriendName(
                                        ofNullable(caseData.getRespondent1LitigationFriend())
                                            .map(LitigationFriend::getFullName)
                                            .orElse(""))
                                    .build());
            }
            return respondents;
        }

        if (respondent2HasSameLegalRep(caseData)) {
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                return List.of(
                    Party.builder()
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
                        .build()
                );
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
                                       .representative(representativeService.getRespondent1Representative(caseData))
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

    private List<Expert> getExpertsDetails(DQ dq) {
        if (dq.getExperts().getDetails() == null) {
            return Collections.emptyList();
        }
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

    private String getHearingLength(DQ dq) {
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
