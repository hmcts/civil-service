package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor
public class DirectionsQuestionnairePreparer {

    private final DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    public CaseData prepareDirectionsQuestionnaire(CaseData caseData, String userToken) {
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        if (shouldPrepareSingleResponse(caseData, scenario)) {
            singleResponseFile(userToken, caseData);
        } else if (respondent2HasSameLegalRep(caseData)) {
            prepareDQForSameLegalRepScenario(caseData, userToken);
        } else {
            prepareDQForDifferentLegalRepScenario(caseData, userToken);
        }
        return caseData;
    }

    private boolean shouldPrepareSingleResponse(CaseData caseData, MultiPartyScenario scenario) {
        return !SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            || uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator.isClaimantResponse(caseData)
            || scenario == MultiPartyScenario.ONE_V_ONE
            || scenario == MultiPartyScenario.TWO_V_ONE;
    }

    private void prepareDQForDifferentLegalRepScenario(CaseData caseData, String userToken) {
        /*
        for MultiParty, when there is a single respondent, this block is executed (when only one respondent
        respondent2SameLegalRepresentative == null, so respondent2HasSameLegalRep(CaseData) == false.
        I'm not sure if that is what should happen, but I'll leave that to a MP ticket
        */
        ArrayList<Element<CaseDocument>> updatedDocuments = new ArrayList<>(caseData.getSystemGeneratedCaseDocuments());
        addDifferentLegalRepDQIfEligible(caseData, userToken, updatedDocuments, "ONE");
        addDifferentLegalRepDQIfEligible(caseData, userToken, updatedDocuments, "TWO");
        caseData.setSystemGeneratedCaseDocuments(updatedDocuments);
    }

    private void addDifferentLegalRepDQIfEligible(CaseData caseData,
                                                  String userToken,
                                                  List<Element<CaseDocument>> updatedDocuments,
                                                  String respondent) {
        if (!shouldGenerateDifferentLegalRepDQ(caseData, respondent)) {
            return;
        }

        directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(caseData, userToken, respondent)
            .ifPresent(document -> addDifferentLegalRepDocument(caseData, updatedDocuments, respondent, document));
    }

    private boolean shouldGenerateDifferentLegalRepDQ(CaseData caseData, String respondent) {
        return switch (respondent) {
            case "ONE" -> caseData.getRespondent1DQ() != null
                && isFullDefenceOrPartAdmission(caseData.getRespondent1ClaimResponseTypeForSpec());
            case "TWO" -> caseData.getRespondent2DQ() != null
                && isFullDefenceOrPartAdmission(caseData.getRespondent2ClaimResponseTypeForSpec());
            default -> false;
        };
    }

    private boolean isFullDefenceOrPartAdmission(RespondentResponseTypeSpec responseType) {
        return responseType == RespondentResponseTypeSpec.FULL_DEFENCE
            || responseType == RespondentResponseTypeSpec.PART_ADMISSION;
    }

    private void addDifferentLegalRepDocument(CaseData caseData,
                                              List<Element<CaseDocument>> updatedDocuments,
                                              String respondent,
                                              CaseDocument document) {
        updatedDocuments.add(element(document));
        if ("ONE".equals(respondent)) {
            caseData.setRespondent1DocumentURL(document.getDocumentLink().getDocumentUrl());
            assignCategoryId.assignCategoryIdToCaseDocument(document, DocCategory.DQ_DEF1.getValue());
            return;
        }
        caseData.setRespondent2DocumentURL(document.getDocumentLink().getDocumentUrl());
        assignCategoryId.assignCategoryIdToCaseDocument(document, DocCategory.DQ_DEF2.getValue());
    }

    private void prepareDQForSameLegalRepScenario(CaseData caseData, String userToken) {
        if (caseData.getRespondentResponseIsSame() == NO) {
            if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1ClaimResponseTypeForSpec() != null
                && caseData.getRespondent1ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
                generateDQ1v2SameSol(caseData, userToken, "ONE");
            }

            if (caseData.getRespondent2DQ() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
                generateDQ1v2SameSol(caseData, userToken, "TWO");
            }
        } else {
            singleResponseFile(
                userToken,
                caseData
            );
        }
    }

    public void generateDQ1v2SameSol(CaseData caseData, String userToken, String sol) {
        CaseDocument directionsQuestionnaire =
            directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(
                caseData,
                userToken,
                sol
            );
        assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.DQ_DEF1.getValue());
        List<Element<CaseDocument>> systemGeneratedCaseDocuments =
            caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseData.setSystemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
    }

    private void singleResponseFile(String bearerToken, CaseData caseData) {
        if (shouldStoreClaimantDqInPreTranslation(caseData)) {
            isClaimantDqPreTranslation(bearerToken, caseData);
            return;
        }

        CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(caseData, bearerToken);
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(directionsQuestionnaire, "");
        categoriseDirectionsQuestionnaire(caseData, directionsQuestionnaire, copy);
        storeDirectionsQuestionnaire(caseData, directionsQuestionnaire);
    }

    private boolean shouldStoreClaimantDqInPreTranslation(CaseData caseData) {
        return featureToggleService.isWelshEnabledForMainCase()
            && caseData.isLRvLipOneVOne()
            && caseData.isRespondentResponseBilingual()
            && CaseState.AWAITING_APPLICANT_INTENTION.equals(caseData.getCcdState());
    }

    private void categoriseDirectionsQuestionnaire(CaseData caseData,
                                                   CaseDocument directionsQuestionnaire,
                                                   CaseDocument copy) {
        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            categoriseUnspecDirectionsQuestionnaire(caseData, directionsQuestionnaire, copy);
            return;
        }
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            categoriseSpecDirectionsQuestionnaire(caseData, directionsQuestionnaire, copy);
        }
    }

    private void categoriseUnspecDirectionsQuestionnaire(CaseData caseData,
                                                         CaseDocument directionsQuestionnaire,
                                                         CaseDocument copy) {
        if (isClaimantDirectionsQuestionnaire(directionsQuestionnaire)) {
            addClaimantCategories(caseData, directionsQuestionnaire, copy);
        }
        if (isDefendantDirectionsQuestionnaire(directionsQuestionnaire)) {
            assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.DQ_DEF1.getValue());
        }
        if (isRespondentTwoGeneratedDocument(caseData, directionsQuestionnaire)) {
            assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.DQ_DEF2.getValue());
        }
    }

    private void categoriseSpecDirectionsQuestionnaire(CaseData caseData,
                                                       CaseDocument directionsQuestionnaire,
                                                       CaseDocument copy) {
        if (isClaimantDirectionsQuestionnaire(directionsQuestionnaire)) {
            addClaimantCategories(caseData, directionsQuestionnaire, copy);
        }
        if (isDefendantDirectionsQuestionnaire(directionsQuestionnaire)) {
            assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.DQ_DEF1.getValue());
        }
    }

    private boolean isClaimantDirectionsQuestionnaire(CaseDocument directionsQuestionnaire) {
        return directionsQuestionnaire.getDocumentName().contains("claimant");
    }

    private boolean isDefendantDirectionsQuestionnaire(CaseDocument directionsQuestionnaire) {
        return directionsQuestionnaire.getDocumentName().contains("defendant");
    }

    private boolean isRespondentTwoGeneratedDocument(CaseData caseData, CaseDocument directionsQuestionnaire) {
        return nonNull(caseData.getRespondent2DocumentGeneration())
            && caseData.getRespondent2DocumentGeneration().equals("userRespondent2")
            && !isClaimantDirectionsQuestionnaire(directionsQuestionnaire);
    }

    private void addClaimantCategories(CaseData caseData, CaseDocument directionsQuestionnaire, CaseDocument copy) {
        assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.APP1_DQ.getValue());
        assignCategoryId.assignCategoryIdToCaseDocument(copy, DocCategory.DQ_APP1.getValue());
        caseData.getDuplicateSystemGeneratedCaseDocs().add(element(copy));
    }

    private void storeDirectionsQuestionnaire(CaseData caseData, CaseDocument directionsQuestionnaire) {
        if (shouldStoreRespondentOriginalDq(caseData)) {
            caseData.setRespondent1OriginalDqDoc(directionsQuestionnaire);
            return;
        }
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseData.setSystemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
    }

    private boolean shouldStoreRespondentOriginalDq(CaseData caseData) {
        return featureToggleService.isWelshEnabledForMainCase()
            && caseData.isLipvLROneVOne()
            && caseData.isClaimantBilingual()
            && CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.equals(caseData.getCcdState());
    }

    private void isClaimantDqPreTranslation(String bearerToken, CaseData caseData) {
        CaseDocument directionsQuestionnairePretranslation = directionsQuestionnaireGenerator.generate(
            caseData,
            bearerToken
        );
        assignCategoryId.assignCategoryIdToCaseDocument(
            directionsQuestionnairePretranslation,
            DocCategory.APP1_DQ.getValue()
        );
        List<Element<CaseDocument>> preTranslationDocuments = caseData.getPreTranslationDocuments();
        preTranslationDocuments.add(element(directionsQuestionnairePretranslation));
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
