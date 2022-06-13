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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.DirectionsQuestionnaireGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
@SuppressWarnings("unchecked")
public class GenerateDirectionsQuestionnaireCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GENERATE_DIRECTIONS_QUESTIONNAIRE,
        GENERATE_DIRECTIONS_QUESTIONNAIRE_SPEC
    );

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
        System.out.println("inside prepareDirectionsQuestionnaire method");
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (respondent2HasSameLegalRep(caseData) && NO == caseData.getRespondentResponseIsSame()) {
            System.out.println("NOT 1v1 process document generation");
            if (isDefendant1DQResponse(caseData)) {
                System.out.println("NOT 1 1v1 process document generation");
                generateAndSet1V2SameSolDivergentResponsesDQ(callbackParams, caseData, caseDataBuilder, "ONE");
            }

            if (isDefendant2DQResponse(caseData)) {
                System.out.println("NOT 2 1v1 process document generation");
                generateAndSet1V2SameSolDivergentResponsesDQ(callbackParams, caseData, caseDataBuilder, "TWO");
            }
        } else {
            System.out.println("1v1 process document generation");
            generateAndSetDQ(callbackParams, caseData, caseDataBuilder);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void generateAndSetDQ(CallbackParams callbackParams, CaseData caseData,
                                  CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
    }

    private boolean isDefendant2DQResponse(CaseData caseData) {
        return caseData.getRespondent2DQ() != null
            && RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType());
    }

    private boolean isDefendant1DQResponse(CaseData caseData) {
        return caseData.getRespondent1DQ() != null
            && RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType());
    }

    private void generateAndSet1V2SameSolDivergentResponsesDQ(CallbackParams callbackParams, CaseData caseData,
                                                              CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                              String defendantIdentifier) {
        CaseDocument directionsQuestionnaire =
            directionsQuestionnaireGenerator.generateDQFor1v2SingleSolDiffResponse(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            defendantIdentifier
        );

        List<Element<CaseDocument>> systemGeneratedCaseDocuments =
            caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
    }

    public void generateDQ1v2SameSol(CallbackParams callbackParams, String sol)
    {
        System.out.println("inside new generateDQ method ");
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
        if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            caseDataBuilder.respondent1GeneratedResponseDocument(directionsQuestionnaire);
        }
    }

    public void generateDQ1v2DiffSolicitor(CallbackParams callbackParams, String sol)
    {
        System.out.println("inside newgenerateDQ1v2DiffSolicitor");
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        Optional<CaseDocument> directionsQuestionnaire =
            directionsQuestionnaireGenerator.generateDQFor1v2DiffSol(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                sol
            );

        directionsQuestionnaire.ifPresent(document -> {
            List<Element<CaseDocument>> systemGeneratedCaseDocuments =
                caseData.getSystemGeneratedCaseDocuments();
            systemGeneratedCaseDocuments.add(element(document));
            caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        });

        //TO DO this will be revisited
        /*if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            caseDataBuilder.respondent1GeneratedResponseDocument(directionsQuestionnaire);
        }*/
    }
    /**
     * Next version for prepareDirectionsQuestionnaire. The main difference is storing the generated document
     * not only in the generated documents list but also in respondent1GeneratedResponseDocument, so we can use
     * easily locate it in intention to proceed journey.
     *
     * @param callbackParams parameters of the callback
     * @return response of the callback
     */
    private CallbackResponse prepareDirectionsQuestionnaireV1(CallbackParams callbackParams) {
        System.out.println("inside prepareDirectionsQuestionnaireV1");
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        boolean claimantResponseLRspec = false;
        if (directionsQuestionnaireGenerator.isClaimantResponse(caseData)
            && SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            claimantResponseLRspec = true;
        }
        System.out.println(" !claimantResponseLRspec value "+ !claimantResponseLRspec);
        if (respondent2HasSameLegalRep(caseData) && !claimantResponseLRspec) {
            System.out.println("inside respondent2HasSameLegalRep(caseData) && !claimantResponseLRspec");
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
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
            if(!claimantResponseLRspec)
            {

                   if (caseData.getRespondent1DQ() != null
                    && caseData.getRespondent1ClaimResponseTypeForSpec() != null
                    && (caseData.getRespondent1ClaimResponseTypeForSpec()
                    .equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                    || caseData.getRespondent1ClaimResponseTypeForSpec()
                       .equals(RespondentResponseTypeSpec.PART_ADMISSION)))
                   {
                    System.out.println(" FULL defence and part admission respondent 1");
                    generateDQ1v2DiffSolicitor(callbackParams, "ONE");

                }

                if (caseData.getRespondent2DQ() != null
                    && caseData.getRespondent2ClaimResponseTypeForSpec() != null
                    && (caseData.getRespondent2ClaimResponseTypeForSpec()
                    .equals(RespondentResponseTypeSpec.FULL_DEFENCE)
                    || caseData.getRespondent2ClaimResponseTypeForSpec()
                    .equals(RespondentResponseTypeSpec.PART_ADMISSION)))
                   {
                    System.out.println(" FULL defence and part admission respondent 2");
                    generateDQ1v2DiffSolicitor(callbackParams, "TWO");
                }
            }
            else {


                singleResponseFile(
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    caseData,
                    caseDataBuilder
                );
            }
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
    private void singleResponseFile(String bearerToken, CaseData caseData,
                                    CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        CaseDocument directionsQuestionnaire = directionsQuestionnaireGenerator.generate(
            caseData,
            bearerToken
        );

        List<Element<CaseDocument>> systemGeneratedCaseDocuments = caseData.getSystemGeneratedCaseDocuments();
        systemGeneratedCaseDocuments.add(element(directionsQuestionnaire));
        caseDataBuilder.systemGeneratedCaseDocuments(systemGeneratedCaseDocuments);
        if (SuperClaimType.SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
            caseDataBuilder.respondent1GeneratedResponseDocument(directionsQuestionnaire);
        }
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
