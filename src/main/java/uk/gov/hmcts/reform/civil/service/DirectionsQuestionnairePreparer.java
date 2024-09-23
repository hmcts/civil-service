package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
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

    public CaseData prepareDirectionsQuestionnaire(CaseData caseData, String userToken) {
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (!SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            || uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator.isClaimantResponse(caseData)
            || scenario == MultiPartyScenario.ONE_V_ONE
            || scenario == MultiPartyScenario.TWO_V_ONE) {
            singleResponseFile(
                userToken,
                caseData,
                caseDataBuilder
            );
        } else if (respondent2HasSameLegalRep(caseData)) {
            prepareDQForSameLegalRepScenario(caseData, userToken, caseDataBuilder);
        } else {
            /*
            for MultiParty, when there is a single respondent, this block is executed (when only one respondent
            respondent2SameLegalRepresentative == null, so respondent2HasSameLegalRep(CaseData) == false.
            I'm not sure if that is what should happen, but I'll leave that to a MP ticket
            */

            ArrayList<Element<CaseDocument>> updatedDocuments =
                new ArrayList<>(caseData.getSystemGeneratedCaseDocuments());

            if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1ClaimResponseTypeForSpec() != null
                && (caseData.getRespondent1ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                || caseData.getRespondent1ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.PART_ADMISSION))) {

                directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(
                    caseData,
                    userToken,
                    "ONE"
                ).ifPresent(document -> {
                    updatedDocuments.add(element(document));
                    caseDataBuilder.respondent1DocumentURL(document.getDocumentLink().getDocumentUrl());
                    assignCategoryId.assignCategoryIdToCaseDocument(document, DocCategory.DQ_DEF1.getValue());
                });
            }

            if (caseData.getRespondent2DQ() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                && (caseData.getRespondent2ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                || caseData.getRespondent2ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.PART_ADMISSION))) {

                directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(
                    caseData,
                    userToken,
                    "TWO"
                ).ifPresent(document -> {
                    updatedDocuments.add(element(document));
                    caseDataBuilder.respondent2DocumentURL(document.getDocumentLink().getDocumentUrl());
                    assignCategoryId.assignCategoryIdToCaseDocument(document, DocCategory.DQ_DEF2.getValue());
                });
            }

            caseDataBuilder.systemGeneratedCaseDocuments(updatedDocuments);
        }
        return caseDataBuilder.build();
    }

    private void prepareDQForSameLegalRepScenario(
        CaseData caseData, String userToken,
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
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
                caseData,
                caseDataBuilder
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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
    }

    private void singleResponseFile(String bearerToken, CaseData caseData,
                                    CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(
            caseData,
            bearerToken
        );
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        List<Element<CaseDocument>> duplicateSystemGeneratedCaseDocs = caseData.getDuplicateSystemGeneratedCaseDocs();
        CaseDocument copy = assignCategoryId.copyCaseDocumentWithCategoryId(directionsQuestionnaire, "");
        String claimant = "claimant";
        if (UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (directionsQuestionnaire.getDocumentName().contains(claimant)) {
                assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.APP1_DQ.getValue());
                assignCategoryId.assignCategoryIdToCaseDocument(copy, DocCategory.DQ_APP1.getValue());
                duplicateSystemGeneratedCaseDocs.add(element(copy));
            }
            if (directionsQuestionnaire.getDocumentName().contains("defendant")) {
                assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.DQ_DEF1.getValue());
            }
            if (nonNull(caseData.getRespondent2DocumentGeneration())
                && caseData.getRespondent2DocumentGeneration().equals("userRespondent2")
                && !directionsQuestionnaire.getDocumentName().contains(claimant)) {
                assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.DQ_DEF2.getValue());
            }
        }
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            if (directionsQuestionnaire.getDocumentName().contains(claimant)) {
                assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.APP1_DQ.getValue());
                assignCategoryId.assignCategoryIdToCaseDocument(copy, DocCategory.DQ_APP1.getValue());
                duplicateSystemGeneratedCaseDocs.add(element(copy));
            }
            if (directionsQuestionnaire.getDocumentName().contains("defendant")) {
                assignCategoryId.assignCategoryIdToCaseDocument(directionsQuestionnaire, DocCategory.DQ_DEF1.getValue());
            }
        }
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
