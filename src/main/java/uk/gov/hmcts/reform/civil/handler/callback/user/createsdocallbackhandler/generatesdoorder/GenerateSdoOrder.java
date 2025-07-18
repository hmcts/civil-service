package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;

@Component
@AllArgsConstructor
@Slf4j
public class GenerateSdoOrder implements CaseTask {

    private final AssignCategoryId assignCategoryId;
    private final ObjectMapper objectMapper;
    private final SdoGeneratorService sdoGeneratorService;
    private final List<GenerateSdoOrderValidator> generateSdoOrderValidators;
    private final List<GenerateSdoOrderCaseDataMapper> generateSdoOrderCaseDataMappers;

    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing GenerateSdoOrder with callback version: {}", callbackParams.getVersion());
        CaseData caseData = V_1.equals(callbackParams.getVersion())
                ? mapHearingMethodFields(callbackParams.getCaseData())
                : callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        log.debug("Handling witness statements");
        generateSdoOrderValidators.forEach(validator -> validator.validate(caseData, errors));
        log.debug("Handling SdoR2 Fast Track");

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        if (errors.isEmpty()) {
            log.info("No errors found, proceeding to document generation");
            handleDocumentGeneration(caseData, callbackParams, updatedData);
        } else {
            log.warn("Errors found: {}", errors);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .data(updatedData.build().toMap(objectMapper))
                .build();
    }

    private void handleDocumentGeneration(CaseData caseData, CallbackParams callbackParams,
                                          CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Generating SDO document");
        CaseDocument document = sdoGeneratorService.generate(
                caseData,
                callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        if (document != null) {
            log.debug("Document generated successfully");
            updatedData.sdoOrderDocument(document);
        } else {
            log.warn("Document generation returned null");
        }
        assignCategoryId.assignCategoryIdToCaseDocument(document, "caseManagementOrders");
        log.debug("Assigned category ID to document");
    }

    private CaseData mapHearingMethodFields(CaseData caseData) {
        log.info("Mapping hearing method fields");
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        generateSdoOrderCaseDataMappers.forEach(mapper -> mapper.mapHearingMethodFields(caseData, updatedData));

        return updatedData.build();
    }
}
