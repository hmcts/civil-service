package uk.gov.hmcts.reform.civil.service.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.CaseDetails;
import uk.gov.hmcts.reform.civil.CaseDefinitionConstants;
import uk.gov.hmcts.reform.civil.config.StitchingConfiguration;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BundleDocument;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.Value;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService.CREATED_BY;

@Service
@RequiredArgsConstructor
@Slf4j
public class CivilDocumentStitchingService implements DocumentStitcher {

    private final BundleRequestExecutor bundleRequestExecutor;
    private final ObjectMapper objectMapper;
    private final StitchingConfiguration stitchingConfiguration;

    public CaseDocument bundle(List<DocumentMetaData> documents, String authorisation, String bundleTitle, String bundleFilename, CaseData caseData) {
        CaseDetails payload = createBundlePayload(documents, bundleTitle, bundleFilename, caseData);
        log.info(
            "Calling stitching api end point for {}, Bundle Title {}, File Name {}",
            caseData.getLegacyCaseReference(),
            bundleTitle,
            bundleFilename
        );

        CaseData caseDataFromBundlePayload = bundleRequestExecutor.post(
            BundleRequest.builder().caseDetails(payload).build(),
            stitchingConfiguration.getStitchingUrl(),
            authorisation
        );
        log.info("Called stitching api end point for {}", caseData.getLegacyCaseReference());
        if (caseDataFromBundlePayload == null) {
            log.info("Case data is null----------");
            return null;
        }
        Optional<Document> stitchedDocument = caseDataFromBundlePayload.getCaseBundles().get(0).getValue().getStitchedDocument();

        log.info("stitchedDocument.isPresent() {}, legacy case reference {}",  stitchedDocument.isPresent(), caseData.getLegacyCaseReference());
        return retrieveCaseDocument(stitchedDocument);

    }

    private CaseDocument retrieveCaseDocument(Optional<Document> stitchedDocument) {
        if (stitchedDocument.isEmpty()) {
            log.info("stitchedDocument is not present----------");
            return null;
        }
        Document document = stitchedDocument.get();
        String documentUrl = document.getDocumentUrl();
        String documentBinaryUrl = document.getDocumentBinaryUrl();

        return CaseDocument.builder()
            .documentLink(Document.builder().documentUrl(documentUrl)
                              .documentBinaryUrl(documentBinaryUrl)
                              .documentFileName(document.getDocumentFileName()).build())
            .documentName("Stitched document")
            .documentType(SEALED_CLAIM)
            .createdDatetime(LocalDateTime.now(ZoneId.of("Europe/London")))
            .createdBy(CREATED_BY)
            .build();
    }

    private CaseDetails createBundlePayload(List<DocumentMetaData> documents, String bundleTitle, String bundleFilename, CaseData caseData) {

        List<IdValue<BundleDocument>> bundleDocuments = prepareBundleDocuments(documents);
        List<Value<Document>> caseDocuments = prepareCaseDocuments(documents);

        List<IdValue<Bundle>> idValueList = new ArrayList<>();
        idValueList.add(new IdValue<>(
            "1",
            Bundle.builder().id("1").description(bundleTitle).eligibleForStitching("yes").documents(bundleDocuments).fileName(bundleFilename).build()
        ));

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseBundles(idValueList);
        caseDataBuilder.caseDocuments(caseDocuments);
        caseDataBuilder.caseDocument1Name(bundleFilename);

        return CaseDetails.builder().id(caseData.getCcdCaseReference())
            .data(caseDataBuilder.build().toMap(objectMapper))
            .caseTypeId(CaseDefinitionConstants.CASE_TYPE)
            .jurisdictionId(CaseDefinitionConstants.JURISDICTION).build();
    }

    private static List<Value<Document>> prepareCaseDocuments(List<DocumentMetaData> documents) {
        return documents.stream()
            .map(caseDocument -> new Value<>(caseDocument.getDocument().getDocumentFileName(), caseDocument.getDocument()))
            .collect(Collectors.toList());
    }

    private static List<IdValue<BundleDocument>> prepareBundleDocuments(List<DocumentMetaData> documents) {
        List<IdValue<BundleDocument>> bundleDocuments = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            DocumentMetaData caseDocument = documents.get(i);
            bundleDocuments.add(new IdValue<>(
                String.valueOf(i),
                new BundleDocument(caseDocument.getDocument().getDocumentFileName(), caseDocument.getDescription(), i, caseDocument.getDocument())
            ));
        }
        return bundleDocuments;
    }
}
