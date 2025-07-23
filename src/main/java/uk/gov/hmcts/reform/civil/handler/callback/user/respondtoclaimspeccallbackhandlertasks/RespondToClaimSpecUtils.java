package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.REPAYMENT_PLAN_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;

@Component
@RequiredArgsConstructor
@Slf4j
public class RespondToClaimSpecUtils {

    static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";
    private static final String DEF2 = "Defendant 2";
    private final LocationReferenceDataService locationRefDataService;
    private final UserService userService;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final FrcDocumentsUtils frcDocumentsUtils;
    private final AssignCategoryId assignCategoryId;
    private final DQResponseDocumentUtils dqResponseDocumentUtils;

    public static void addRespondentDocuments(CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads,
                                              ResponseDocument respondent1SpecDefenceResponseDocument, AssignCategoryId assignCategoryId) {
        log.info("Adding respondent documents for caseId: {}", updatedCaseData.build().getCcdCaseReference());

        if (respondent1SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1SpecDefenceResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                log.debug("CaseId {}: Adding Respondent 1 claim document", updatedCaseData.build().getCcdCaseReference());
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent1ClaimDocument, "Defendant",
                        updatedCaseData.build().getRespondent1ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                );
                assignCategoryId.assignCategoryIdToDocument(
                        respondent1ClaimDocument,
                        DocCategory.DEF1_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }

        log.info("CaseId {}: Respondent documents addition complete", updatedCaseData.build().getCcdCaseReference());
    }

    public boolean isRespondent2HasSameLegalRep(CaseData caseData) {
        log.info("Checking if Respondent 2 has the same legal representative for caseId: {}", caseData.getCcdCaseReference());
        return caseData.getRespondent2SameLegalRepresentative() != null
                && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    public List<LocationRefData> getLocationData(CallbackParams callbackParams) {
        log.info("Retrieving court locations for default judgments for caseId: {}", callbackParams.getCaseData().getCcdCaseReference());
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    public boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Checking if solicitor represents only one of the respondents for caseId: {}", caseData.getCcdCaseReference());
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
                && coreCaseUserService.userHasCaseRole(
                caseData.getCcdCaseReference().toString(),
                userInfo.getUid(),
                caseRole
        );
    }

    public Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        log.info("Determining who disputes based on part admission for caseId: {}", caseData.getCcdCaseReference());
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

        switch (mpScenario) {
            case ONE_V_ONE:
                log.debug("CaseId {}: Handling ONE_V_ONE scenario", caseData.getCcdCaseReference());
                handleOneVOneScenario(caseData, tags);
                break;
            case TWO_V_ONE:
                log.debug("CaseId {}: Handling TWO_V_ONE scenario", caseData.getCcdCaseReference());
                handleTwoVOneScenario(caseData, tags);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                log.debug("CaseId {}: Handling ONE_V_TWO_ONE_LEGAL_REP scenario", caseData.getCcdCaseReference());
                handleOneVTwoOneLegalRepScenario(caseData, tags);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                log.debug("CaseId {}: Handling ONE_V_TWO_TWO_LEGAL_REP scenario", caseData.getCcdCaseReference());
                handleOneVTwoTwoLegalRepScenario(caseData, tags);
                break;
            default:
                log.error("CaseId {}: Unknown multi-party scenario", caseData.getCcdCaseReference());
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }

        log.info("CaseId {}: Determination of who disputes based on part admission complete", caseData.getCcdCaseReference());
        return tags;
    }

    private void handleOneVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.info("Handling ONE_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("CaseId {}: Respondent 1 disputes in ONE_V_ONE scenario", caseData.getCcdCaseReference());
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleTwoVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.info("Handling TWO_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());

        if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
                && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)
                || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("CaseId {}: Adding ONLY_RESPONDENT_1_DISPUTES tag", caseData.getCcdCaseReference());
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.info("Handling ONE_V_TWO_ONE_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("CaseId {}: Respondent 1 disputes in ONE_V_TWO_ONE_LEGAL_REP scenario", caseData.getCcdCaseReference());
            if (caseData.getRespondentResponseIsSame() == YES
                    || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                log.debug("CaseId {}: Adding BOTH_RESPONDENTS_DISPUTE tag", caseData.getCcdCaseReference());
                tags.add(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE);
            } else {
                log.debug("CaseId {}: Adding ONLY_RESPONDENT_1_DISPUTES tag", caseData.getCcdCaseReference());
                tags.add(ONLY_RESPONDENT_1_DISPUTES);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("CaseId {}: Adding ONLY_RESPONDENT_2_DISPUTES tag", caseData.getCcdCaseReference());
            tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.info("Handling ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)
                && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("CaseId {}: Adding ONLY_RESPONDENT_1_DISPUTES tag", caseData.getCcdCaseReference());
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        } else if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)
                && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            log.debug("CaseId {}: Adding ONLY_RESPONDENT_2_DISPUTES tag", caseData.getCcdCaseReference());
            tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    public void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags) {
        tags.removeIf(EnumSet.of(
                ONLY_RESPONDENT_1_DISPUTES,
                DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
                DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE,
                SOMEONE_DISPUTES,
                DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL,
                DefendantResponseShowTag.RESPONDENT_1_PAID_LESS,
                DefendantResponseShowTag.RESPONDENT_2_PAID_LESS,
                WHEN_WILL_CLAIM_BE_PAID,
                RESPONDENT_1_ADMITS_PART_OR_FULL,
                RESPONDENT_2_ADMITS_PART_OR_FULL,
                NEED_FINANCIAL_DETAILS_1,
                NEED_FINANCIAL_DETAILS_2,
                DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY,
                WHY_2_DOES_NOT_PAY_IMMEDIATELY,
                REPAYMENT_PLAN_2,
                DefendantResponseShowTag.MEDIATION
        )::contains);
    }

    public void assembleResponseDocumentsSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Assembling response documents for caseId: {}", caseData.getCcdCaseReference());

        List<Element<CaseDocument>> defendantUploads = getDefendantUploads(caseData, updatedCaseData);
        log.debug("CaseId {}: Retrieved defendant uploads", caseData.getCcdCaseReference());

        List<Element<CaseDocument>> additionalDocuments = dqResponseDocumentUtils.buildDefendantResponseDocuments(updatedCaseData.build());
        log.debug("CaseId {}: Built additional response documents", caseData.getCcdCaseReference());

        defendantUploads.addAll(additionalDocuments);
        log.debug("CaseId {}: Added additional documents to defendant uploads", caseData.getCcdCaseReference());

        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
            log.debug("CaseId {}: Updated case data with defendant response documents", caseData.getCcdCaseReference());
        }

        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);
        log.debug("CaseId {}: Assembled defendants' FRC documents", caseData.getCcdCaseReference());

        clearTempDocuments(updatedCaseData);
        log.info("CaseId {}: Response documents assembly complete", caseData.getCcdCaseReference());
    }

    private List<Element<CaseDocument>> getDefendantUploads(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Retrieving defendant uploads for caseId: {}", caseData.getCcdCaseReference());

        List<Element<CaseDocument>> defendantUploads = nonNull(caseData.getDefendantResponseDocuments())
                ? caseData.getDefendantResponseDocuments() : new ArrayList<>();
        log.debug("CaseId {}: Initialized defendant uploads list", caseData.getCcdCaseReference());

        addRespondent1Documents(caseData, updatedCaseData, defendantUploads);
        log.debug("CaseId {}: Added Respondent 1 documents to defendant uploads", caseData.getCcdCaseReference());

        addRespondent2Documents(caseData, updatedCaseData, defendantUploads);
        log.debug("CaseId {}: Added Respondent 2 documents to defendant uploads", caseData.getCcdCaseReference());

        log.info("CaseId {}: Defendant uploads retrieval complete", caseData.getCcdCaseReference());
        return defendantUploads;
    }

    private void addRespondent1Documents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        log.info("Adding Respondent 1 documents for caseId: {}", caseData.getCcdCaseReference());
        ResponseDocument respondent1SpecDefenceResponseDocument = caseData.getRespondent1SpecDefenceResponseDocument();
        addRespondentDocuments(
                updatedCaseData,
                defendantUploads,
                respondent1SpecDefenceResponseDocument,
                assignCategoryId
        );
    }

    private void addRespondent2Documents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        log.info("Adding Respondent 2 documents for caseId: {}", caseData.getCcdCaseReference());

        ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
        if (respondent2SpecDefenceResponseDocument != null) {
            log.debug("CaseId {}: Respondent 2 defence response document is not null", caseData.getCcdCaseReference());
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
            if (respondent2ClaimDocument != null) {
                log.debug("CaseId {}: Adding Respondent 2 claim document", caseData.getCcdCaseReference());
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent2ClaimDocument, DEF2,
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                );
                assignCategoryId.assignCategoryIdToDocument(
                        respondent2ClaimDocument,
                        DocCategory.DEF2_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
                log.debug("CaseId {}: Added Respondent 2 claim document to defendant uploads", caseData.getCcdCaseReference());
                addCopyIfNonNull(defendantUploads, documentElement);
            }
        }
    }

    private void addCopyIfNonNull(List<Element<CaseDocument>> defendantUploads, Element<CaseDocument> documentElement) {
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(documentElement.getValue(), DocCategory.DQ_DEF2.getValue());
        if (Objects.nonNull(copy)) {
            defendantUploads.add(ElementUtils.element(copy));
        }
    }

    private void clearTempDocuments(CaseData.CaseDataBuilder<?, ?> builder) {
        log.info("Clearing temporary documents for caseId: {}", builder.build().getCcdCaseReference());

        CaseData caseData = builder.build();
        builder.respondent1SpecDefenceResponseDocument(null);
        log.debug("CaseId {}: Cleared Respondent 1 Spec Defence Response Document", caseData.getCcdCaseReference());

        builder.respondent2SpecDefenceResponseDocument(null);
        log.debug("CaseId {}: Cleared Respondent 2 Spec Defence Response Document", caseData.getCcdCaseReference());

        if (nonNull(caseData.getRespondent1DQ())) {
            builder.respondent1DQ(builder.build().getRespondent1DQ().toBuilder().respondent1DQDraftDirections(null).build());
            log.debug("CaseId {}: Cleared Respondent 1 DQ Draft Directions", caseData.getCcdCaseReference());
        }
        if (nonNull(caseData.getRespondent2DQ())) {
            builder.respondent2DQ(builder.build().getRespondent2DQ().toBuilder().respondent2DQDraftDirections(null).build());
            log.debug("CaseId {}: Cleared Respondent 2 DQ Draft Directions", caseData.getCcdCaseReference());
        }
    }
}
