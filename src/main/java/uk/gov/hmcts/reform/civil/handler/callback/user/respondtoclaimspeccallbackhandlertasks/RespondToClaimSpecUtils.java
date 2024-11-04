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

    private final LocationReferenceDataService locationRefDataService;
    private final UserService userService;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final FrcDocumentsUtils frcDocumentsUtils;
    private final AssignCategoryId assignCategoryId;
    private final DQResponseDocumentUtils dqResponseDocumentUtils;

    private static final String DEF2 = "Defendant 2";

    public boolean isRespondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    public List<LocationRefData> getLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    public boolean isSolicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    public Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

        switch (mpScenario) {
            case ONE_V_ONE:
                handleOneVOneScenario(caseData, tags);
                break;
            case TWO_V_ONE:
                handleTwoVOneScenario(caseData, tags);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                handleOneVTwoOneLegalRepScenario(caseData, tags);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                handleOneVTwoTwoLegalRepScenario(caseData, tags);
                break;
            default:
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        return tags;
    }

    private void handleOneVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleTwoVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)
            || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            if (caseData.getRespondentResponseIsSame() == YES
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                tags.add(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE);
            } else {
                tags.add(ONLY_RESPONDENT_1_DISPUTES);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        } else if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)
            && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
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
        List<Element<CaseDocument>> defendantUploads = getDefendantUploads(caseData, updatedCaseData);
        List<Element<CaseDocument>> additionalDocuments = dqResponseDocumentUtils.buildDefendantResponseDocuments(updatedCaseData.build());
        defendantUploads.addAll(additionalDocuments);

        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }

        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);
        clearTempDocuments(updatedCaseData);
    }

    private List<Element<CaseDocument>> getDefendantUploads(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        List<Element<CaseDocument>> defendantUploads = nonNull(caseData.getDefendantResponseDocuments())
            ? caseData.getDefendantResponseDocuments() : new ArrayList<>();

        addRespondent1Documents(caseData, updatedCaseData, defendantUploads);
        addRespondent2Documents(caseData, updatedCaseData, defendantUploads);

        return defendantUploads;
    }

    private void addRespondent1Documents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        ResponseDocument respondent1SpecDefenceResponseDocument = caseData.getRespondent1SpecDefenceResponseDocument();
        addRespondentDocuments(
            updatedCaseData,
            defendantUploads,
            respondent1SpecDefenceResponseDocument,
            assignCategoryId
        );
    }

    public static void addRespondentDocuments(CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads,
                                              ResponseDocument respondent1SpecDefenceResponseDocument, AssignCategoryId assignCategoryId) {
        if (respondent1SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1SpecDefenceResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
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
    }

    private void addRespondent2Documents(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData, List<Element<CaseDocument>> defendantUploads) {
        ResponseDocument respondent2SpecDefenceResponseDocument = caseData.getRespondent2SpecDefenceResponseDocument();
        if (respondent2SpecDefenceResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2SpecDefenceResponseDocument.getFile();
            if (respondent2ClaimDocument != null) {
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
        CaseData caseData = builder.build();
        builder.respondent1SpecDefenceResponseDocument(null);
        builder.respondent2SpecDefenceResponseDocument(null);

        if (nonNull(caseData.getRespondent1DQ())) {
            builder.respondent1DQ(builder.build().getRespondent1DQ().toBuilder().respondent1DQDraftDirections(null).build());
        }
        if (nonNull(caseData.getRespondent2DQ())) {
            builder.respondent2DQ(builder.build().getRespondent2DQ().toBuilder().respondent2DQDraftDirections(null).build());
        }
    }
}
