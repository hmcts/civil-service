package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseCategoryUtils.isSpecCaseCategory;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateDirectionsQuestionnaireCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GENERATE_DIRECTIONS_QUESTIONNAIRE
    );

    private final DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::prepareDirectionsQuestionnaire
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    public void generateDQ1v2SameSol(CallbackParams callbackParams, String sol) {

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseDocument directionsQuestionnaire =
            directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                sol
            );

        List<Element<CaseDocument>> systemGeneratedCaseDocuments =
            caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            caseDataBuilder.respondent1GeneratedResponseDocument(directionsQuestionnaire);
        }
    }

    /**
     * Next version for prepareDirectionsQuestionnaire. The main difference is storing the generated document
     * not only in the generated documents list but also in respondent1GeneratedResponseDocument, so we can use
     * easily locate it in intention to proceed journey.
     *
     * @param callbackParams parameters of the callback
     * @return response of the callback
     */

    private CallbackResponse prepareDirectionsQuestionnaire(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        if (caseData.getCaseAccessCategory() != CaseCategory.SPEC_CLAIM
            || DirectionsQuestionnaireGenerator.isClaimantResponse(caseData)
            || scenario == MultiPartyScenario.ONE_V_ONE
            || scenario == MultiPartyScenario.TWO_V_ONE) {
            singleResponseFile(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                caseData,
                caseDataBuilder
            );
        } else if (respondent2HasSameLegalRep(caseData)) {
            prepareDQForSameLegalRepScenario(callbackParams, caseData, caseDataBuilder);
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
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    "ONE"
                ).ifPresent(document -> {
                    updatedDocuments.add(element(document));
                    caseDataBuilder.respondent1GeneratedResponseDocument(document);
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
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    "TWO"
                ).ifPresent(document -> {
                    updatedDocuments.add(element(document));
                    caseDataBuilder.respondent2GeneratedResponseDocument(document);
                });
            }

            caseDataBuilder.systemGeneratedCaseDocuments(updatedDocuments);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void prepareDQForSameLegalRepScenario(CallbackParams callbackParams,
                                                  CaseData caseData,
                                                  CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (caseData.getRespondentResponseIsSame() == NO) {
            if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1ClaimResponseTypeForSpec() != null
                && caseData.getRespondent1ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
                generateDQ1v2SameSol(callbackParams, "ONE");
            }

            if (caseData.getRespondent2DQ() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                && caseData.getRespondent2ClaimResponseTypeForSpec()
                .equals(RespondentResponseTypeSpec.FULL_DEFENCE)) {
                generateDQ1v2SameSol(callbackParams, "TWO");
            }
        } else {
            singleResponseFile(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                caseData,
                caseDataBuilder
            );
        }
    }

    /**
     * Generates a file for single response and adds it to the system generated files list
     * and as the respondent1 generated response file.
     *
     * @param bearerToken     bearer token to generate files
     * @param caseData        current case data
     * @param caseDataBuilder builder for the modified case data
     */
    private void singleResponseFile(String bearerToken, CaseData caseData,
                                    CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(
            caseData,
            bearerToken
        );

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        if (isSpecCaseCategory(caseData, featureToggleService.isAccessProfilesEnabled())) {
            caseDataBuilder.respondent1GeneratedResponseDocument(directionsQuestionnaire);
        }
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
