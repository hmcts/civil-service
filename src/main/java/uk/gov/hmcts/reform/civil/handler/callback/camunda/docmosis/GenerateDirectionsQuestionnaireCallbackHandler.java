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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_QUESTIONNAIRE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_DIRECTIONS_QUESTIONNAIRE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class GenerateDirectionsQuestionnaireCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(GENERATE_DIRECTIONS_QUESTIONNAIRE,
                                                          GENERATE_DIRECTIONS_QUESTIONNAIRE_SPEC);

    private final DirectionsQuestionnaireGenerator directionsQuestionnaireGenerator;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::prepareDirectionsQuestionnaireV1,
            callbackKey(ABOUT_TO_SUBMIT), this::prepareDirectionsQuestionnaire
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareDirectionsQuestionnaire(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        if (respondent2HasSameLegalRep(caseData)) {
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                if (caseData.getRespondent1DQ() != null
                    && caseData.getRespondent1ClaimResponseType() != null
                    && caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
                    CaseDocument directionsQuestionnaire =
                        directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(
                            caseData,
                            callbackParams.getParams().get(BEARER_TOKEN).toString(),
                            "ONE"
                        );

                    List<Element<CaseDocument>> systemGeneratedCaseDocuments =
                        caseData.getSystemGeneratedCaseDocuments();
                    systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
                    caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
                }

                if (caseData.getRespondent2DQ() != null
                    && caseData.getRespondent2ClaimResponseType() != null
                    && caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
                    CaseDocument directionsQuestionnaire =
                        directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(
                            caseData,
                            callbackParams.getParams().get(BEARER_TOKEN).toString(),
                            "TWO"
                        );

                    List<Element<CaseDocument>> systemGeneratedCaseDocuments =
                        caseData.getSystemGeneratedCaseDocuments();
                    systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
                    caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
                }
            } else {
                // TODO explore the possibility of this being redundant and remove if so
                CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(
                    caseData,
                    callbackParams.getParams().get(BEARER_TOKEN).toString()
                );

                List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
                systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
                caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
            }
        } else {
            CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString()
            );

            List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
            systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
            caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse prepareDirectionsQuestionnaireV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        if (respondent2HasSameLegalRep(caseData)) {
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                if (caseData.getRespondent1DQ() != null
                    && caseData.getRespondent1ClaimResponseType() != null
                    && caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
                    CaseDocument directionsQuestionnaire =
                        directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(
                            caseData,
                            callbackParams.getParams().get(BEARER_TOKEN).toString(),
                            "ONE"
                        );

                    List<Element<CaseDocument>> systemGeneratedCaseDocuments =
                        caseData.getSystemGeneratedCaseDocuments();
                    systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
                    caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
                    caseDataBuilder.respondent1GeneratedResponseDocument(directionsQuestionnaire);
                }

                if (caseData.getRespondent2DQ() != null
                    && caseData.getRespondent2ClaimResponseType() != null
                    && caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
                    CaseDocument directionsQuestionnaire =
                        directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(
                            caseData,
                            callbackParams.getParams().get(BEARER_TOKEN).toString(),
                            "TWO"
                        );

                    List<Element<CaseDocument>> systemGeneratedCaseDocuments =
                        caseData.getSystemGeneratedCaseDocuments();
                    systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
                    caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
                    caseDataBuilder.respondent2GeneratedResponseDocument(directionsQuestionnaire);
                }
            } else {
                // TODO explore the possibility of this being redundant and remove if so
                singleResponseFile(
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    caseData,
                    caseDataBuilder
                );
            }
        } else {
            /*
            for MultiParty, when there is a single respondent, this block is executed (when only one respondent
            respondent2SameLegalRepresentative == null, so respondent2HasSameLegalRep(CaseData) == false.
            I'm not sure if that is what should happen, but I'll leave that to a MP ticket
             */
            singleResponseFile(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                caseData,
                caseDataBuilder
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    /**
     * Generates a file for single response and adds it to the system generated files list
     * and as the respondent1 generated response file.
     *
     * @param bearerToken     bearer token to generate files
     * @param caseData        current case data
     * @param caseDataBuilder builder for the modified case data
     */
    private void singleResponseFile(String bearerToken, CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(
            caseData,
            bearerToken
        );

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        caseDataBuilder.respondent1GeneratedResponseDocument(directionsQuestionnaire);
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
