package uk.gov.hmcts.reform.civil.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeep;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

@AllArgsConstructor
@Service
@Slf4j
public class DocumentRemovalService {

    public static final String DOCUMENT_FILENAME = "document_filename";
    static final String DOCUMENT_URL = "document_url";
    private static final String DOCUMENT_BINARY_URL = "document_binary_url";
    private static final String DOCUMENT_UPLOAD_TIMESTAMP = "upload_timestamp";
    private static final String VALUE_KEY = "value";
    private final ObjectMapper objectMapper;
    private final DocumentManagementService documentManagementService;

    public List<DocumentToKeepCollection> getCaseDocumentsList(CaseData caseData) {
        JsonNode root = objectMapper.valueToTree(caseData);
        List<JsonNode> documentNodes = new ArrayList<>();

        log.info(format("Retrieving document JSON nodes for case id %s", caseData.getCcdCaseReference()));
        retrieveDocumentNodes(root, documentNodes);
        log.info(format("Building case document list for case id %s", caseData.getCcdCaseReference()));
        return buildCaseDocumentList(documentNodes);
    }

    public CaseData removeDocuments(CaseData caseData, Long caseId, String userAuthorisation) {
        List<DocumentToKeepCollection> allExistingDocumentsList = getCaseDocumentsList(caseData);

        ArrayList<DocumentToKeepCollection> documentsUserWantsDeletedList = new ArrayList<>(allExistingDocumentsList);
        Optional<List<DocumentToKeepCollection>> documentsUserWantsToKeepList = Optional.ofNullable(caseData.getDocumentToKeepCollection());
        documentsUserWantsToKeepList.ifPresent(documentsUserWantsDeletedList::removeAll);

        log.info(format("Beginning removal of %s document from Case ID %s", documentsUserWantsDeletedList.size(), caseId));

        documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
            deleteDocument(
                documentToDeleteCollection.getValue(), userAuthorisation));

        JsonNode caseDataJson = objectMapper.valueToTree(caseData);

        documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
            removeDocumentFromJson(
                caseDataJson, documentToDeleteCollection.getValue()));

        log.info(format("Document removal complete, removing DocumentToKeep collection "
            + "from CaseData JSON for case ID: %s", caseId));

        ((ObjectNode) caseDataJson).remove("documentToKeepCollection");

        return buildAmendedCaseDataFromRootNode(caseDataJson, caseId);
    }

    private String getUploadTimestampFromDocumentNode(JsonNode documentNode) {
        String documentNodeUploadTimestamp;
        try {
            documentNodeUploadTimestamp =
                Objects.isNull(documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP)) ? null :
                    documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP).asText();
        } catch (Exception e) {
            log.error(format(
                "Error getting upload timestamp for document url: %s.",
                documentNode.get(DOCUMENT_URL).asText()));
            documentNodeUploadTimestamp = null;
        }
        return documentNodeUploadTimestamp;
    }

    private List<DocumentToKeepCollection> buildCaseDocumentList(List<JsonNode> documentNodes) {

        List<DocumentToKeepCollection> documentsCollection = new ArrayList<>();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length - 1];

            documentsCollection.add(
                DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                        .documentId(docId)
                        .caseDocument(CaseDocument.builder()
                            .documentLink(Document.builder()
                                .documentFileName(documentNode.get(DOCUMENT_FILENAME).asText())
                                .documentUrl(documentNode.get(DOCUMENT_URL).asText())
                                .documentBinaryUrl(documentNode.get(DOCUMENT_BINARY_URL).asText())
                                .uploadTimestamp(getUploadTimestampFromDocumentNode(documentNode))
                                .build())
                            .build())
                        .build())
                    .build());
        }

        documentsCollection.sort(Comparator.comparing(
            DocumentToKeepCollection::getValue,
            Comparator.comparing(DocumentToKeep::getCaseDocument,
                Comparator.comparing(CaseDocument::getDocumentLink,
                    Comparator.comparing(Document::getUploadTimestamp,
                        Comparator.nullsLast(
                            Comparator.reverseOrder()))))));

        return documentsCollection.stream().distinct().toList();
    }

    private void retrieveDocumentNodes(JsonNode root, List<JsonNode> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)) {
                    documentNodes.add(fieldValue);
                } else {
                    retrieveDocumentNodes(fieldValue, documentNodes);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                retrieveDocumentNodes(arrayElement, documentNodes);
            }
        }
    }

    private void removeDocumentFromJson(JsonNode root, DocumentToKeep documentToDelete) {
        List<String> fieldsToRemove = new ArrayList<>();

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (shouldRemoveDocument(fieldValue,
                    documentToDelete.getCaseDocument().getDocumentLink().getDocumentUrl())) {
                    log.info(String.format("Deleting doc with url %s",
                        documentToDelete.getCaseDocument().getDocumentLink().getDocumentUrl()));
                    fieldsToRemove.add(fieldName);
                } else {
                    removeDocumentFromJson(fieldValue, documentToDelete);
                }
            }
        } else if (root.isArray()) {
            processArrayNode(root, documentToDelete);
        }

        for (String fieldName : fieldsToRemove) {
            ((ObjectNode) root).remove(fieldName);
        }
    }

    private void processArrayNode(JsonNode root, DocumentToKeep documentToDelete) {
        ArrayNode arrayNode = (ArrayNode) root;
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode arrayElement = arrayNode.get(i);
            if (arrayElement.has(VALUE_KEY)) {
                JsonNode valueObject = arrayElement.get(VALUE_KEY);
                Iterator<String> fieldNames = valueObject.fieldNames();

                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode fieldValue = valueObject.get(fieldName);

                    final String documentUrl = documentToDelete.getCaseDocument().getDocumentLink().getDocumentUrl();
                    if (fieldValue.asText().equals(
                        documentUrl)
                        || shouldRemoveDocument(fieldValue,
                        documentUrl)) {
                        log.info(String.format("Deleting doc with url %s", documentUrl));
                        ((ArrayNode) root).remove(i);
                    }
                }
            }
            removeDocumentFromJson(arrayElement, documentToDelete);
        }
    }

    private boolean shouldRemoveDocument(JsonNode fieldValue, String documentToRemoveUrl) {
        return fieldValue.has(DOCUMENT_URL)
            && fieldValue.get(DOCUMENT_URL).asText().equals(documentToRemoveUrl);
    }

    private void deleteDocument(DocumentToKeep documentToKeep, String authorisationToken) {
        String documentUrl = documentToKeep.getCaseDocument().getDocumentLink().getDocumentUrl();
        try {
            log.info(String.format("Deleting doc from DocStore with url %s",
                documentUrl));

            documentManagementService.deleteDocument(authorisationToken, documentUrl);
        } catch (Exception e) {
            log.error(format(
                "Failed to delete document url %s",
                documentUrl), e);

            throw new DocumentDeleteException(e.getMessage(), e);
        }
    }

    private CaseData buildAmendedCaseDataFromRootNode(JsonNode root, Long caseId) {
        CaseData amendedCaseData;
        try {
            log.info(format("Building amendedCaseData for case id %s after deleting document", caseId));
            amendedCaseData = objectMapper.treeToValue(root, CaseData.class);
        } catch (Exception e) {
            log.error(format("Error building amendedCaseData for case id %s after deleting document", caseId), e);
            throw new DocumentDeleteException(e.getMessage(), e);
        }
        return amendedCaseData;
    }
}
