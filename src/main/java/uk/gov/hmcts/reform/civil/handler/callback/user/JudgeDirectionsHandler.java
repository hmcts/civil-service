package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DocumentFileType;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.DynamicMultiSelectList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sdo.SdoDocumentFormDisposal;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.documentmerge.DocumentMergingService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.documentmerge.MergeDoc;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIMANT_DRAFT_DIRECTIONS;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENDANT_DEFENCE;

@Service
@RequiredArgsConstructor
public class JudgeDirectionsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
            CaseEvent.DIRECTIONS_SPIKE
    );

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentMergingService documentMergingservice;
    private final DocumentHearingLocationHelper locationHelper;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_START), this::aboutToStart,
                callbackKey(MID, "create-directions-template"), this::createDirectionsTemplate,
                callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toBuilder()
                        .directionsCreationMethod(DynamicMultiSelectList.builder()
                                .listItems(buildDirectionDocumentsList(caseData))
                                .build())
                        .build().toMap(objectMapper))
                .build();
    }

    List<Element<CaseDocument>> getAllPartyDirectionDocuments(CaseData caseData) {
        return Stream.concat(caseData.getClaimantResponseDocuments().stream(), caseData.getDefendantResponseDocuments().stream())
                .filter(item -> isDirectionsType(item.getValue().getDocumentType())).toList();
    }

    List<DynamicListElement> buildDirectionDocumentsList(CaseData caseData) {
        return getAllPartyDirectionDocuments(caseData).stream()
                .map(item -> DynamicListElement.builder()
                        .code(item.getId().toString())
                        .label(getListItemLabel(item.getValue())).build())
                .collect(Collectors.toList());
    }

    private String getListItemLabel(CaseDocument caseDocument) {
        boolean createdByParty = nonNull(caseDocument.getCreatedBy()) && List.of("Defendant", "Claimant").contains(caseDocument.getCreatedBy());
        return createdByParty ? caseDocument.getCreatedBy() + " Directions" : caseDocument.getDocumentName();
    }

    private boolean isDirectionsType(DocumentType type) {
        //For some reason the defendants directions document is uploaded as type DEFENDANT_DEFENCE???
        return type.equals(CLAIMANT_DRAFT_DIRECTIONS) || type.equals(DEFENDANT_DEFENCE);
    }

    private CallbackResponse createDirectionsTemplate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var auth = callbackParams.getParams().get(BEARER_TOKEN).toString();

        byte[] directionsTemplate = generateEmptyDocumentWithHeader(auth, caseData).getBytes();

        if (nonNull(caseData.getDirectionsCreationMethod().getValue())) {
            List<String> selectedDocumentIds = caseData.getDirectionsCreationMethod().getValue().stream().map(item -> item.getCode()).toList();
            List<MergeDoc> selectedDocumentLinks =
                    getAllPartyDirectionDocuments(caseData).stream()
                            .filter(item -> selectedDocumentIds.contains(item.getId().toString()))
                            .map(item -> MergeDoc.builder().sectionHeader(getListItemLabel(item.getValue()))
                                    .file(documentManagementService.downloadDocument(auth, getDocumentLink(item.getValue())))
                                    .build()
                            ).toList();

            List<MergeDoc> filesToMerge = Stream.concat(
                    List.of(MergeDoc.builder().file(directionsTemplate).build()).stream(),
                    selectedDocumentLinks.stream()).toList();
            directionsTemplate = documentMergingservice.merge(filesToMerge);
        }

        CaseDocument document = documentManagementService.uploadDocument(
                auth,
                new PDF(
                        "directions-template.docx",
                        directionsTemplate,
                        DocumentType.DIRECTION_ORDER
                )
        );

        CaseData updated = caseData.toBuilder()
                .judgeDraftDirections(document.getDocumentLink())
                .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updated.toMap(objectMapper))
                .build();
    }

    DocmosisDocument generateEmptyDocumentWithHeader(String auth, CaseData caseData) {
        //ToDo: Create dedicated DocumentForm, currently using SDO form as it uses the same heading markup.
        return documentGeneratorService.generateDocmosisDocument(
                SdoDocumentFormDisposal.builder()
                        .caseNumber(caseData.getLegacyCaseReference())
                        .writtenByJudge(true)
                        .caseManagementLocation(locationHelper.getHearingLocation(null, caseData, auth))
                        .caseNumber(caseData.getLegacyCaseReference())
                        .applicant1(caseData.getApplicant1())
                        .hasApplicant2(SdoHelper.hasSharedVariable(caseData, "applicant2"))
                        .applicant2(caseData.getApplicant2())
                        .respondent1(caseData.getRespondent1())
                        .hasRespondent2(SdoHelper.hasSharedVariable(caseData, "respondent2"))
                        .respondent2(caseData.getRespondent2())
                        .build(), DocmosisTemplates.DIRECTIONS_TEMPLATE_DOC, DocumentFileType.DOCX);
    }

    private String getDocumentLink(CaseDocument caseDocument) {
        return caseDocument.getDocumentLink().getDocumentUrl().substring(
                caseDocument.getDocumentLink().getDocumentUrl().lastIndexOf("/") + 1);
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var directions = caseData.getJudgeDirections();

        //ToDo Upload directions document and set CaseDocument in whichever document list in case data.

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toBuilder()
                        .directionsCreationMethod(null)
                        .judgeDraftDirections(null)
                        .judgeDirections(null)
                        .build().toMap(objectMapper)).build();
    }
}
