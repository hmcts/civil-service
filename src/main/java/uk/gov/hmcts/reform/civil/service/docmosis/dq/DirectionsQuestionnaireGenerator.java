package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_2V1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_CLAIMANT_MULTIPARTY_DIFF_SOLICITOR;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N181_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor
public class DirectionsQuestionnaireGenerator implements TemplateDataGenerator<DirectionsQuestionnaireForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final StateFlowEngine stateFlowEngine;
    private final RepresentativeService representativeService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        DocmosisTemplates templateId;
        DocmosisDocument docmosisDocument;
        DirectionsQuestionnaireForm templateData;
        if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            if (isClaimantResponse(caseData)) {
                templateId = DocmosisTemplates.CLAIMANT_RESPONSE_SPEC;
            } else {
                templateId = DocmosisTemplates.DEFENDANT_RESPONSE_SPEC;
            }
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
        DocmosisTemplates templateId = N181;
        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (isClaimantResponse(caseData) && isClaimantMultipartyProceed(caseData)) {
                    templateId = N181_CLAIMANT_MULTIPARTY_DIFF_SOLICITOR;
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                if (!isClaimantResponse(caseData)
                    || (isClaimantResponse(caseData) && isClaimantMultipartyProceed(caseData))) {
                    templateId = N181_MULTIPARTY_SAME_SOL;
                }
                break;
            case TWO_V_ONE:
                if (!isClaimantResponse(caseData)
                    || (isClaimantResponse(caseData) && isClaimantMultipartyProceed(caseData))) {
                    templateId = N181_2V1;
                }
                break;
            default:
        }
        return templateId;
    }

    public CaseDocument generateDQFor1v2SingleSolDiffResponse(CaseData caseData,
                                                              String authorisation,
                                                              String respondent) {
        DocmosisTemplates templateId = TWO_V_ONE.equals(MultiPartyScenario
                                                            .getMultiPartyScenario(caseData)) ? N181_2V1 : N181;
        DirectionsQuestionnaireForm templateData;

        if (respondent.equals("ONE")) {
            templateData = getRespondent1TemplateData(caseData, "ONE");
        } else if (respondent.equals("TWO")) {
            templateData = getRespondent2TemplateData(caseData, "TWO");
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

    // return optional, if you get an empty optional, you didn't need to generate the doc
    public Optional<CaseDocument> generateDQFor1v2DiffSol(CaseData caseData,
                                                          String authorisation,
                                                          String respondent) {
        // TODO check if this is the correct template, I just copy-pasted from generateDQFor1v2SingleSolDiffResponse
        DocmosisTemplates templateId = TWO_V_ONE.equals(MultiPartyScenario
                                                            .getMultiPartyScenario(caseData)) ? N181_2V1 : N181;
        String fileName = getFileName(caseData, templateId);
        LocalDateTime responseDate;
        if ("ONE".equals(respondent)) {
            responseDate = caseData.getRespondent1ResponseDate();
        } else if ("TWO".equals(respondent)) {
            responseDate = caseData.getRespondent2ResponseDate();
        } else {
            throw new IllegalArgumentException("Respondent argument is expected to be one of ONE or TWO");
        }
        if (responseDate == null) {
            throw new NullPointerException("Response date should not be null");
        }
        if (caseData.getSystemGeneratedCaseDocuments().stream()
            .anyMatch(element ->
                          Objects.equals(element.getValue().getCreatedDatetime(), responseDate)
                              && fileName.equals(element.getValue().getDocumentName()))) {
            // this DQ is already generated
            return Optional.empty();
        }

        DirectionsQuestionnaireForm templateData;
        if (respondent.equals("ONE")) {
            templateData = getRespondent1TemplateData(caseData, "ONE");
        } else {
            // TWO
            templateData = getRespondent2TemplateData(caseData, "TWO");
        }

        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(templateData, N181);
        CaseDocument document = documentManagementService.uploadDocument(
            authorisation,
            new PDF(getFileName(caseData, templateId), docmosisDocument.getBytes(),
                    DocumentType.DIRECTIONS_QUESTIONNAIRE
            )
        );
        // set the create date time equal to the response date time, so we can check it afterwards
        return Optional.of(document.toBuilder().createdDatetime(responseDate).build());
    }

    private String getFileName(CaseData caseData, DocmosisTemplates templateId) {
        String userPrefix = isRespondentState(caseData) ? "defendant" : "claimant";
        return String.format(templateId.getDocumentTitle(), userPrefix, caseData.getLegacyCaseReference());
    }

    private DQ getDQAndSetSubmittedOn(DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder,
                                      CaseData caseData) {
        if (isClaimantResponse(caseData)) {
            if (onlyApplicant2IsProceeding(caseData)) {
                builder.submittedOn(caseData.getApplicant2ResponseDate().toLocalDate());
                return caseData.getApplicant2DQ();
            } else {
                builder.submittedOn(caseData.getApplicant1ResponseDate().toLocalDate());
                return caseData.getApplicant1DQ();
            }
        } else {
            if (isRespondent2(caseData)) {
                builder.submittedOn(caseData.getRespondent2ResponseDate().toLocalDate());
                return caseData.getRespondent2DQ();
            } else {
                builder.submittedOn(caseData.getRespondent1ResponseDate().toLocalDate());
                return caseData.getRespondent1DQ();
            }
        }
    }

    @Override
    public DirectionsQuestionnaireForm getTemplateData(CaseData caseData) {

        boolean claimantResponseLRspec = isClaimantResponse(caseData)
            && SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType());

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils
                                     .fetchSolicitorReferences(caseData))
            .respondents(getRespondents(caseData, null))
            .applicants(claimantResponseLRspec ? getApplicants(caseData) : null)
            .allocatedTrack(caseData.getAllocatedTrack());

        if (!SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            builder.statementOfTruthText(createStatementOfTruthText(isRespondentState(caseData)));
        }
        DQ dq = getDQAndSetSubmittedOn(builder, caseData);

        if (!claimantResponseLRspec) {
            setApplicants(builder, caseData);
        }

        Witnesses witnesses = getWitnesses(dq);

        Integer witnessesIncludingDefendants = null;
        String state = stateFlowEngine.evaluate(caseData).getState().getName();
        if (!(SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())
            && state.equals(FULL_ADMISSION.fullName()))) {
            witnessesIncludingDefendants = countWitnessesIncludingDefendant(witnesses, caseData);
        }

        boolean specAndSmallClaim = false;
        if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())
            && "SMALL_CLAIM".equals(caseData.getResponseClaimTrack())) {
            witnesses = getWitnessesSmallClaim(witnessesIncludingDefendants);
            specAndSmallClaim = true;
        }

        builder.fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .disclosureOfElectronicDocuments(dq.getDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(dq.getDisclosureOfNonElectronicDocuments())
            .experts(!specAndSmallClaim ? getExperts(dq) : getSmallClaimExperts(dq, caseData))
            .witnesses(witnesses)
            .witnessesIncludingDefendants(witnessesIncludingDefendants)
            .hearing(getHearing(dq))
            .hearingSupport(getHearingSupport(dq))
            .furtherInformation(getFurtherInformation(dq, caseData))
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .disclosureReport(getDisclosureReport(dq))
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .requestedCourt(getRequestedCourt(dq));

        return builder.build();
    }

    private List<Party> getApplicants(CaseData caseData) {
        var applicant = caseData.getApplicant1();
        var applicant2 = caseData.getApplicant2();
        var respondentRepresentative = representativeService.getApplicantRepresentative(caseData);
        var litigationFriend = caseData.getRespondent1LitigationFriend();
        if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
                return List.of(Party.builder()
                                   .name(applicant.getPartyName())
                                   .primaryAddress(applicant.getPrimaryAddress())
                                   .representative(respondentRepresentative)
                                   .litigationFriendName(
                                       ofNullable(litigationFriend)
                                           .map(LitigationFriend::getFullName)
                                           .orElse(""))
                                   .build(),
                               Party.builder()
                                   .name(applicant2.getPartyName())
                                   .primaryAddress(applicant2.getPrimaryAddress())
                                   .representative(respondentRepresentative)
                                   .litigationFriendName(
                                       ofNullable(litigationFriend)
                                           .map(LitigationFriend::getFullName)
                                           .orElse(""))
                                   .build());
            }
        }
        return List.of(Party.builder()
                           .name(applicant.getPartyName())
                           .primaryAddress(applicant.getPrimaryAddress())
                           .representative(respondentRepresentative)
                           .litigationFriendName(
                               ofNullable(litigationFriend)
                                   .map(LitigationFriend::getFullName)
                                   .orElse(""))
                           .build());
    }

    private Party getApplicant2DQParty(CaseData caseData) {

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

    private Party getApplicant1DQParty(CaseData caseData) {
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

    private void setApplicants(DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder,
                               CaseData caseData) {
        if (TWO_V_ONE.equals(MultiPartyScenario
                                 .getMultiPartyScenario(caseData))) {
            if (onlyApplicant2IsProceeding(caseData)) {
                builder.applicant(getApplicant2DQParty(caseData));
            } else {
                builder.applicant(getApplicant1DQParty(caseData));
                builder.applicant2(getApplicant2DQParty(caseData));
            }
        } else {
            builder.applicant(getApplicant1DQParty(caseData));
        }
    }

    private int countWitnessesIncludingDefendant(Witnesses witnesses, CaseData caseData) {
        int witnessesIncludingDefendants;
        if (AllocatedTrack.SMALL_CLAIM.equals(caseData.getAllocatedTrack())
            || SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            String smallClaimNumberOfWitnesses = caseData.getResponseClaimWitnesses();
            if (isClaimantResponse(caseData)) {
                smallClaimNumberOfWitnesses = caseData.getApplicant1ClaimWitnesses();
            }
            if (StringUtils.isNotBlank(smallClaimNumberOfWitnesses)
                && smallClaimNumberOfWitnesses.matches("\\d+")) {
                witnessesIncludingDefendants = Integer.parseInt(smallClaimNumberOfWitnesses);
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

        String furtherJudgeInfo = Stream.of(
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

    public static boolean isClaimantResponse(CaseData caseData) {
        return "CLAIMANT_RESPONSE".equals(ofNullable(caseData.getBusinessProcess())
                                              .map(BusinessProcess::getCamundaEvent)
                                              .orElse(null))
                || "CLAIMANT_RESPONSE_SPEC".equals(ofNullable(caseData.getBusinessProcess())
                                              .map(BusinessProcess::getCamundaEvent)
                                              .orElse(null));
    }

    private boolean isClaimantMultipartyProceed(CaseData caseData) {
        return (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                    && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())) // 2v1 scenario
            || (YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()) // 1v2 scenario
                    && YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()));
    }

    private boolean onlyApplicant2IsProceeding(CaseData caseData) {
        return !YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
    }

    private DirectionsQuestionnaireForm getRespondent2TemplateData(CaseData caseData, String defendantIdentifier) {
        DQ dq = caseData.getRespondent2DQ();

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .submittedOn(caseData.getRespondent2ResponseDate().toLocalDate())
            .applicant(getApplicant1DQParty(caseData))
            .respondents(getRespondents(caseData, defendantIdentifier))
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
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .allocatedTrack(caseData.getAllocatedTrack())
            .build();
    }

    private DirectionsQuestionnaireForm getRespondent1TemplateData(CaseData caseData, String defendantIdentifier) {
        DQ dq = caseData.getRespondent1DQ();

        return DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils.fetchSolicitorReferences(caseData))
            .submittedOn(caseData.getRespondent1ResponseDate().toLocalDate())
            .applicant(getApplicant1DQParty(caseData))
            .respondents(getRespondents(caseData, defendantIdentifier))
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
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
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
            || state.equals(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName())
            || state.equals(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName());
    }

    private boolean isRespondent2(CaseData caseData) {
        if (caseData.getRespondent2ResponseDate() != null) {
            return caseData.getRespondent1ResponseDate() == null
                || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate());
        }

        return false;
    }

    private boolean isProceedingAgainstRespondent1(CaseData caseData) {
        return YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            || YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
            || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2());
    }

    private boolean isProceedingAgainstRespondent2(CaseData caseData) {
        return YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
    }

    private List<Party> getRespondents(CaseData caseData, String defendantIdentifier) {
        if (isClaimantResponse(caseData)) {

            List<Party> respondents = new ArrayList<>();

            if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())
                && !ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
                if ((ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                    && YES.equals(caseData.getRespondentResponseIsSame()))
                    || (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))
                    && RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                    && RespondentResponseTypeSpec.FULL_DEFENCE
                    .equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
                    ) {
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
                    respondents.add(Party.builder()
                                        .name(caseData.getRespondent2().getPartyName())
                                        .primaryAddress(caseData.getRespondent2().getPrimaryAddress())
                                        .representative(representativeService
                                                            .getRespondent2Representative(caseData))
                                        .litigationFriendName(
                                            ofNullable(caseData.getRespondent2LitigationFriend())
                                                .map(LitigationFriend::getFullName)
                                                .orElse(""))
                                        .build());
                } else if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
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
                return respondents;
            }

            if (isProceedingAgainstRespondent1(caseData)) {
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
            if (isProceedingAgainstRespondent2(caseData)) {
                respondents.add(Party.builder()
                                    .name(caseData.getRespondent2().getPartyName())
                                    .primaryAddress(caseData.getRespondent2().getPrimaryAddress())
                                    .representative(representativeService
                                                        .getRespondent2Representative(caseData))
                                    .litigationFriendName(
                                        ofNullable(caseData.getRespondent2LitigationFriend())
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
                            ofNullable(caseData.getRespondent2LitigationFriend())
                                .map(LitigationFriend::getFullName)
                                .orElse(""))
                        .build()
                );
            } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                if ("ONE".equals(defendantIdentifier)) {
                    // TODO add specific test to check below party
                    return List.of(Party.builder()
                                       .name(caseData.getRespondent1().getPartyName())
                                       .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                                       .representative(representativeService.getRespondent1Representative(caseData))
                                       .litigationFriendName(
                                           ofNullable(caseData.getRespondent1LitigationFriend())
                                               .map(LitigationFriend::getFullName)
                                               .orElse(""))
                                       .build());
                } else if ("TWO".equals(defendantIdentifier)) {
                    return List.of(Party.builder()
                                       .name(caseData.getRespondent2().getPartyName())
                                       .primaryAddress(caseData.getRespondent2().getPrimaryAddress())
                                       .representative(representativeService.getRespondent1Representative(caseData))
                                       .litigationFriendName(
                                           ofNullable(caseData.getRespondent2LitigationFriend())
                                               .map(LitigationFriend::getFullName)
                                               .orElse(""))
                                       .build());
                }
            }
        }
        var respondent = caseData.getRespondent1();
        var respondentRepresentative = representativeService.getRespondent1Representative(caseData);
        var litigationFriend = caseData.getRespondent1LitigationFriend();
        if (isRespondent2(caseData)) {
            respondent = caseData.getRespondent2();
            respondentRepresentative = representativeService.getRespondent2Representative(caseData);
            litigationFriend = caseData.getRespondent2LitigationFriend();
        }
        return List.of(Party.builder()
                           .name(respondent.getPartyName())
                           .primaryAddress(respondent.getPrimaryAddress())
                           .representative(respondentRepresentative)
                           .litigationFriendName(
                               ofNullable(litigationFriend)
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

    private Experts getSmallClaimExperts(DQ dq, CaseData caseData) {
        var experts = dq.getSmallClaimExperts();
        YesOrNo expertRequired = caseData.getResponseClaimExpertSpecRequired();
        if (isClaimantResponse(caseData)) {
            expertRequired = caseData.getApplicant1ClaimExpertSpecRequired();
        }
        Expert expertDetails;
        if (experts != null) {
            expertDetails = Expert.builder()
                .name(experts.getExpertName())
                .formattedCost(MonetaryConversions.penniesToPounds(experts.getEstimatedCost()).toString())
                .fieldOfExpertise(experts.getFieldofExpertise())
                .build();
        } else {
            expertDetails = Expert.builder().build();
        }

        return Experts.builder()
            .expertRequired(expertRequired)
            .expertReportsSent(null)
            .jointExpertSuitable(null)
            .details(List.of(expertDetails))
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

    private Witnesses getWitnessesSmallClaim(Integer witnessesIncludingDefendants) {
        if (witnessesIncludingDefendants != null
            && witnessesIncludingDefendants > 0) {
            return Witnesses.builder().witnessesToAppear(YES)
                .details(Collections.emptyList())
                .build();
        }
        return Witnesses.builder().witnessesToAppear(NO)
            .details(Collections.emptyList())
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

    private String createStatementOfTruthText(Boolean respondentState) {
        String role = respondentState ? "defendant" : "claimant";
        String statementOfTruth = role.equals("defendant")
            ? "The defendant believes that the facts stated in the response are true."
            : "The claimant believes that the facts in this claim are true.";
        statementOfTruth += String.format("\n\n\nI am duly authorised by the %s to sign this statement.\n\n"
                                              + "The %s understands that the proceedings for contempt of court "
                                              + "may be brought against anyone who makes, or causes to be made, "
                                              + "a false statement in a document verified by a statement of truth "
                                              + "without an honest belief in its truth.",
                                          IntStream.range(0, 2).mapToObj(i -> role).toArray());
        return statementOfTruth;
    }

}
