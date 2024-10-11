package uk.gov.hmcts.reform.civil.service.docmosis.dq.builders;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.docmosis.FixedRecoverableCostsSection;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Experts;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.FutureApplications;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.GetRespondentsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.SetApplicantsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
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
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.CIVIL_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor
public class DQGeneratorFormBuilder {

    private final IStateFlowEngine stateFlowEngine;
    private final RepresentativeService representativeService;
    private final FeatureToggleService featureToggleService;
    private final LocationReferenceDataService locationRefDataService;
    private final GetRespondentsForDQGenerator respondentsForDQGeneratorTask;
    private final SetApplicantsForDQGenerator setApplicantsForDQGenerator;
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

        Witnesses witnesses = getWitnesses(dq);

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
            .experts(!specAndSmallClaim ? getExperts(dq) : getSmallClaimExperts(dq, caseData, null))
            .witnesses(witnesses)
            .witnessesIncludingDefendants(witnessesIncludingDefendants)
            .hearing(getHearing(dq))
            //Remove hearingSupport after hnl released
            .hearingSupport(getHearingSupport(dq))
            .support(dq.getHearingSupport())
            .furtherInformation(getFurtherInformation(dq, caseData))
            .welshLanguageRequirements(getWelshLanguageRequirements(dq))
            .statementOfTruth(dq.getStatementOfTruth())
            .disclosureReport(shouldDisplayDisclosureReport(caseData) ? dq.getDisclosureReport() : null)
            .vulnerabilityQuestions(dq.getVulnerabilityQuestions())
            .requestedCourt(getRequestedCourt(dq, authorisation));
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

    private Boolean isRespondentState(CaseData caseData) {
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
            || state.equals(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED.fullName())
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

    private Experts getSmallClaimExperts(DQ dq, CaseData caseData, String defendantIdentifier) {
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
                //ToDo: Remove redundant name mapping when hnl toggle removed
                .name(experts.getExpertName())
                //===========================================================
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

    private List<Expert> getExpertsDetails(DQ dq) {
        if (dq.getExperts().getDetails() == null) {
            return Collections.emptyList();
        }
        return unwrapElements(dq.getExperts().getDetails())
            .stream()
            .map(expert -> Expert.builder()
                //ToDo: Remove redundant name mapping when hnl toggle removed
                .name(expert.getName())
                //===========================================================
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

    protected RequestedCourt getRequestedCourt(DQ dq, String authorisation) {
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
