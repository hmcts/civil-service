package uk.gov.hmcts.reform.civil.service.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocumentToKeep;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeep;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;

@Service
@Slf4j
public class DocumentRemovalService {

    protected static final String CASE_DOCUMENT = "CaseDocument";
    protected static final String DOCUMENT_FILENAME = "document_filename";
    protected static final String DOCUMENT_URL = "document_url";
    protected static final String DOCUMENT_BINARY_URL = "document_binary_url";
    protected static final String DOCUMENT_UPLOAD_TIMESTAMP = "upload_timestamp";
    protected static final String DOCUMENT_UPLOADED_BY = "createdBy";
    protected static final String VALUE_KEY = "value";
    protected static final String CIVIL = "Civil";
    private final ObjectMapper objectMapper;

    public DocumentRemovalService(ObjectMapper objectMapper, DocumentManagementService documentManagementService,
                                  @Value("${docStore.doc.removal.enabled:false}") boolean docStoreRemovalEnabled) {
        this.objectMapper = objectMapper;
        this.documentManagementService = documentManagementService;
        this.docStoreRemovalEnabled = docStoreRemovalEnabled;
    }

    private final DocumentManagementService documentManagementService;

    @Value("${docStore.doc.removal.enabled:false}")
    private final boolean docStoreRemovalEnabled;

    public List<DocumentToKeepCollection> getCaseDocumentsList(CaseData caseData) {
        JsonNode root = objectMapper.valueToTree(caseData);
        Map<JsonNode, String> documentNodes = new HashMap<>();

        log.info(format("Retrieving system generated document JSON nodes for case id %s", caseData.getCcdCaseReference()));
        retrieveDocumentNodes(root, documentNodes);
        log.info(format("Building system generated case document list for case id %s", caseData.getCcdCaseReference()));
        return buildCaseDocumentList(documentNodes);
    }

    public CaseData removeDocuments(CaseData caseData, Long caseId, String userAuthorisation) {
        List<DocumentToKeepCollection> allExistingDocumentsList = getCaseDocumentsList(caseData);

        ArrayList<DocumentToKeepCollection> documentsUserWantsDeletedList = new ArrayList<>(allExistingDocumentsList);
        Optional<List<DocumentToKeepCollection>> documentsUserWantsToKeepList = Optional.ofNullable(caseData.getDocumentToKeepCollection());
        documentsUserWantsToKeepList.ifPresent(documentsUserWantsDeletedList::removeAll);

        log.info(format("Beginning removal of %s document from Case ID %s", documentsUserWantsDeletedList.size(), caseId));

        if (docStoreRemovalEnabled) {
            documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
                deleteDocument(
                    documentToDeleteCollection.getValue(), userAuthorisation));
        }

        JsonNode caseDataJson = objectMapper.valueToTree(caseData);

        documentsUserWantsDeletedList.forEach(documentToDeleteCollection ->
            removeDocumentFromJson(
                caseDataJson, documentToDeleteCollection.getValue()));

        log.info(format("Document removal complete, removing DocumentToKeep collection "
            + "from CaseData JSON for case ID: %s", caseId));

        ((ObjectNode) caseDataJson).remove("documentToKeepCollection");

        return buildAmendedCaseDataFromRootNode(caseDataJson, caseId);
    }

    private LocalDateTime getUploadTimestampFromDocumentNode(JsonNode documentNode) {
        LocalDateTime documentNodeUploadTimestamp;
        try {
            documentNodeUploadTimestamp =
                Objects.isNull(documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP)) ? null :
                    LocalDateTime.parse(documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP).asText());
        } catch (Exception e) {
            log.error(format(
                "Error getting upload timestamp for document url: %s.",
                documentNode.get(DOCUMENT_URL).asText()));
            documentNodeUploadTimestamp = null;
        }
        return documentNodeUploadTimestamp;
    }

    private boolean getSystemGeneratedFlag(String field) {
        return CIVIL.equalsIgnoreCase(field);
    }

    private List<DocumentToKeepCollection> buildCaseDocumentList(Map<JsonNode, String> documentNodes) {

        List<DocumentToKeepCollection> documentsCollection = new ArrayList<>();

        for (Map.Entry<JsonNode, String> documentNode : documentNodes.entrySet()) {
            String docUrl = documentNode.getKey().get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length - 1];

            documentsCollection.add(
                    DocumentToKeepCollection.builder()
                            .value(DocumentToKeep.builder()
                                    .documentId(docId)
                                    .caseDocumentToKeep(CaseDocumentToKeep.builder()
                                            .documentFilename(documentNode.getKey().get(DOCUMENT_FILENAME).asText())
                                            .documentUrl(documentNode.getKey().get(DOCUMENT_URL).asText())
                                            .documentBinaryUrl(documentNode.getKey().get(DOCUMENT_BINARY_URL).asText())
                                            .uploadTimestamp(getUploadTimestampFromDocumentNode(documentNode.getKey()))
                                            .build())
                                    .uploadedDate(getUploadTimestampFromDocumentNode(documentNode.getKey()))
                                    .isSystemGenerated(getSystemGeneratedFlag(documentNode.getValue()))
                                    .build())
                            .build());
        }

        documentsCollection.sort(Comparator.comparing(
                DocumentToKeepCollection::getValue,
                Comparator.comparing(DocumentToKeep::getCaseDocumentToKeep,
                        Comparator.comparing(CaseDocumentToKeep::getUploadTimestamp,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder())))));

        return documentsCollection.stream().distinct().toList();
    }

    private void retrieveDocumentNodes(JsonNode root, Map<JsonNode, String> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)) {
                    JsonNode createdByNode = root.get(DOCUMENT_UPLOADED_BY);
                    String createdBy = "null";
                    if (Objects.nonNull(createdByNode)) {
                        createdBy = createdByNode.asText();
                    }
                    documentNodes.put(fieldValue, createdBy);
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
                    documentToDelete.getCaseDocumentToKeep().getDocumentUrl())) {
                    log.info(String.format("Deleting doc with url %s",
                        documentToDelete.getCaseDocumentToKeep().getDocumentUrl()));
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

                    final String documentUrl = documentToDelete.getCaseDocumentToKeep().getDocumentUrl();
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
        String documentUrl = documentToKeep.getCaseDocumentToKeep().getDocumentUrl();
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
