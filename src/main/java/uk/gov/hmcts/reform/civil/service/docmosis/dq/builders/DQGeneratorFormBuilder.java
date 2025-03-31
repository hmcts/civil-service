package uk.gov.hmcts.reform.civil.service.docmosis.dq.builders;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.FutureApplications;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.GetRespondentsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.RespondentTemplateForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.SetApplicantsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor
public class DQGeneratorFormBuilder {

    private final IStateFlowEngine stateFlowEngine;
    private final RepresentativeService representativeService;
    private final FeatureToggleService featureToggleService;
    private final GetRespondentsForDQGenerator respondentsForDQGeneratorTask;
    private final SetApplicantsForDQGenerator setApplicantsForDQGenerator;
    private final RespondentTemplateForDQGenerator respondentTemplateForDQGenerator;
    static final String DEFENDANT = "defendant";
    static final String SMALL_CLAIM = "SMALL_CLAIM";
    static final String organisationName = "Organisation name";

    @NotNull
    public DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder getDirectionsQuestionnaireFormBuilder(CaseData caseData, String authorisation) {
        boolean claimantResponseLRspec = isClaimantResponse(caseData)
            && SPEC_CLAIM.equals(caseData.getCaseAccessCategory());

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = DirectionsQuestionnaireForm.builder()
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .referenceNumber(caseData.getLegacyCaseReference())
            .solicitorReferences(DocmosisTemplateDataUtils
                                     .fetchSolicitorReferences(caseData))
            .respondents(respondentsForDQGeneratorTask.getRespondents(caseData, null))
            .applicants(claimantResponseLRspec ? getApplicants(caseData) : null)
            .allocatedTrack(getClaimTrack(caseData));

        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            builder.statementOfTruthText(createStatementOfTruthText(isRespondentState(caseData)));
        }
        DQ dq = getDQAndSetSubmittedOn(builder, caseData);

        if (!claimantResponseLRspec) {
            setApplicantsForDQGenerator.setApplicants(builder, caseData);
        }

        Witnesses witnesses = respondentTemplateForDQGenerator.getWitnesses(dq);

        Integer witnessesIncludingDefendants = null;
        String state = stateFlowEngine.evaluate(caseData).getState().getName();
        if (!(SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && state.equals(FULL_ADMISSION.fullName()))) {
            witnessesIncludingDefendants = countWitnessesIncludingDefendant(witnesses, caseData);
        }

        boolean specAndSmallClaim = false;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            specAndSmallClaim = true;
        }

        builder.fileDirectionsQuestionnaire(dq.getFileDirectionQuestionnaire())
            .fixedRecoverableCosts(FixedRecoverableCostsSection.from(INTERMEDIATE_CLAIM.toString().equals(getClaimTrack(caseData))
                                                                         ? dq.getFixedRecoverableCostsIntermediate()
                                                                         : dq.getFixedRecoverableCosts()))
            .disclosureOfElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                 ? dq.getDisclosureOfElectronicDocuments() : dq.getSpecDisclosureOfElectronicDocuments())
            .disclosureOfNonElectronicDocuments(UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                    ? dq.getDisclosureOfNonElectronicDocuments() : dq.getSpecDisclosureOfNonElectronicDocuments())
            .deterWithoutHearingYesNo((specAndSmallClaim && claimantResponseLRspec) ? caseData.getDeterWithoutHearing().getDeterWithoutHearingYesNo() : null)
            .deterWithoutHearingWhyNot((specAndSmallClaim && claimantResponseLRspec) ? caseData.getDeterWithoutHearing().getDeterWithoutHearingWhyNot() : null)
            .experts(!specAndSmallClaim ? respondentTemplateForDQGenerator.getExperts(dq) : respondentTemplateForDQGenerator.getSmallClaimExperts(dq, caseData, null))
            .witnesses(witnesses)
            .witnessesIncludingDefendants(witnessesIncludingDefendants)
            .hearing(getHearing(dq))
            //Remove hearingSupport after hnl released
            .hearingSupport(respondentTemplateForDQGenerator.getHearingSupport(dq))
            .support(dq.getHearingSupport())
            .furtherInformation(getFurtherInformation(dq, caseData))
            .welshLanguageRequirements(respondentTemplateForDQGenerator.getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .disclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .requestedCourt(respondentTemplateForDQGenerator.getRequestedCourt(dq, authorisation));
        return builder;
    }

    public static boolean isClaimantResponse(CaseData caseData) {
        var businessProcess = ofNullable(caseData.getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent)
            .orElse(null);
        return "CLAIMANT_RESPONSE".equals(businessProcess)
            || "CLAIMANT_RESPONSE_SPEC".equals(businessProcess)
            || "CLAIMANT_RESPONSE_CUI".equals(businessProcess);
    }

    protected List<Party> getApplicants(CaseData caseData) {
        var legalRepHeading = caseData.getCaseAccessCategory().equals(SPEC_CLAIM) ? "Name" : organisationName;
        var applicant = caseData.getApplicant1();
        var applicant2 = caseData.getApplicant2();
        var respondentRepresentative = representativeService.getApplicantRepresentative(caseData);
        var litigationFriend = caseData.getRespondent1LitigationFriend();
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return List.of(Party.builder()
                               .name(applicant.getPartyName())
                               .emailAddress(caseData.getApplicant1().getPartyEmail())
                               .phoneNumber(caseData.getApplicant1().getPartyPhone())
                               .primaryAddress(applicant.getPrimaryAddress())
                               .representative(respondentRepresentative)
                               .litigationFriendName(
                                   ofNullable(litigationFriend)
                                       .map(LitigationFriend::getFullName)
                                       .orElse(""))
                               .legalRepHeading(legalRepHeading)
                               .build(),
                           Party.builder()
                               .name(applicant2.getPartyName())
                               .emailAddress(caseData.getApplicant2().getPartyEmail())
                               .phoneNumber(caseData.getApplicant2().getPartyPhone())
                               .primaryAddress(applicant2.getPrimaryAddress())
                               .representative(respondentRepresentative)
                               .litigationFriendName(
                                   ofNullable(litigationFriend)
                                       .map(LitigationFriend::getFullName)
                                       .orElse(""))
                               .legalRepHeading(legalRepHeading)
                               .build());
        }
        return List.of(Party.builder()
                           .name(applicant.getPartyName())
                           .emailAddress(applicant.getPartyEmail())
                           .phoneNumber(applicant.getPartyPhone())
                           .primaryAddress(applicant.getPrimaryAddress())
                           .representative(respondentRepresentative)
                           .litigationFriendName(
                               ofNullable(litigationFriend)
                                   .map(LitigationFriend::getFullName)
                                   .orElse(""))
                           .phoneNumber(applicant.getPartyPhone())
                           .emailAddress(applicant.getPartyEmail())
                           .legalRepHeading(legalRepHeading)
                           .build());
    }

    private String getClaimTrack(CaseData caseData) {
        return UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? caseData.getAllocatedTrack().name() : caseData.getResponseClaimTrack();
    }

    private String createStatementOfTruthText(Boolean respondentState) {
        String role = Boolean.TRUE.equals(respondentState) ? DEFENDANT : "claimant";
        String statementOfTruth = role.equals(DEFENDANT)
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

    public Boolean isRespondentState(CaseData caseData) {
        if (isClaimantResponse(caseData)) {
            return false;
        }
        String state = stateFlowEngine.evaluate(caseData).getState().getName();

        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION
            || state.equals(FULL_DEFENCE.fullName())
            || state.equals(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())
            || state.equals(ALL_RESPONSES_RECEIVED.fullName())
            || state.equals(DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName())
            || state.equals(AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED.fullName())
            || state.equals(AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName())
            || state.equals(RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL.fullName());
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
        Respondent1DQ respondent1dq = null;
        if (dq instanceof Respondent1DQ r1dq) {
            respondent1dq = r1dq;
        }
        Optional<FutureApplications> r1dqFutureApplications = ofNullable(respondent1dq)
            .map(Respondent1DQ::getFutureApplications);

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

    private Hearing getHearing(DQ dq) {
        var hearing = dq.getHearing();
        if (hearing != null) {
            return Hearing.builder()
                .hearingLength(respondentTemplateForDQGenerator.getHearingLength(dq))
                .unavailableDatesRequired(hearing.getUnavailableDatesRequired())
                .unavailableDates(unwrapElements(hearing.getUnavailableDates()))
                .build();
        } else {
            return null;
        }
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

    private boolean isRespondent2(CaseData caseData) {
        if (caseData.getRespondent2ResponseDate() != null) {
            return caseData.getRespondent1ResponseDate() == null
                || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate());
        }

        return false;
    }

    private boolean onlyApplicant2IsProceeding(CaseData caseData) {
        return !YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
    }
}
