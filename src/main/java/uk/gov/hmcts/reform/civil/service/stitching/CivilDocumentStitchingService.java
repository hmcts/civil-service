package uk.gov.hmcts.reform.civil.service.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.StitchingConfiguration;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BundleDocument;
import uk.gov.hmcts.reform.civil.model.BundleRequest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.documentmanagement.UnsecuredDocumentManagementService.CREATED_BY;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class CivilDocumentStitchingService implements DocumentStitcher {

    private final BundleRequestExecutor bundleRequestExecutor;
    private final ObjectMapper objectMapper;
    private final StitchingConfiguration stitchingConfiguration;

    public CaseDocument bundle(
        List<DocumentMetaData> documents,
        String authorisation,
        String bundleTitle,
        String bundleFilename,
        CaseData caseData
    ) {
        CaseDocument caseDocument = null;
        CaseDetails payload =
            createBundlePayload(
                documents,
                bundleTitle,
                bundleFilename,
                caseData
            );

        CaseData caseData1 =
            bundleRequestExecutor.post(
                BundleRequest.builder().caseDetails(payload).build(),
                stitchingConfiguration.getStitchingUrl(),
                authorisation
            );
        if (caseData1 != null) {
            Optional<Document> stitchedDocument = caseData1.getCaseBundles().get(0).getValue().getStitchedDocument();

            if (stitchedDocument.isPresent()) {
                Document document = stitchedDocument.get();
                String documentUrl = document.getDocumentUrl();
                String documentBinaryUrl = document.getDocumentBinaryUrl();
                caseDocument = CaseDocument.builder()
                    .documentLink(Document.builder()
                                      .documentUrl("http://dm-store:8080/"
                                                       + documentUrl.substring(documentUrl.indexOf("documents/")))
                                      .documentBinaryUrl("http://dm-store:8080/"
                                                             + documentBinaryUrl.substring(documentBinaryUrl.indexOf(
                                          "documents/")))
                                      .documentFileName(document.getDocumentFileName())
                                      .build())
                    .documentName("Stitched document")
                    .documentType(SEALED_CLAIM)
                    .createdDatetime(LocalDateTime.now())
                    .createdBy(CREATED_BY)
                    .build();
            }
        }
        return caseDocument;
    }

    private CaseDetails createBundlePayload(
        List<DocumentMetaData> documents,
        String bundleTitle,
        String bundleFilename,
        CaseData caseData
    ) {

        List<IdValue<BundleDocument>> bundleDocuments = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {

            DocumentMetaData caseDocument = documents.get(i);

            bundleDocuments.add(
                new IdValue<>(
                    String.valueOf(i),
                    new BundleDocument(
                        caseDocument.getDocument().getDocumentFileName(),
                        caseDocument.getDescription(),
                        i,
                        caseDocument.getDocument()
                    )
                )
            );
        }

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        List<IdValue<Bundle>> idValueList = new ArrayList<>();
        idValueList.add(new IdValue<>(
            "1",
            new Bundle(
                "1",
                bundleTitle,
                "",
                "yes",
                bundleDocuments,
                bundleFilename
            )
        ));

        List<Bundle> bundleList = new ArrayList<>();
        bundleList.add(
            new Bundle(
                "1",
                bundleTitle,
                "",
                "yes",
                bundleDocuments,
                bundleFilename
            )
        );
        caseDataBuilder.caseBundles(idValueList);

        Map<String, Object> data = Map.of("case_details", caseDataBuilder.build().toMap(
            objectMapper));

        return CaseDetails.builder().id(caseData.getCcdCaseReference())
            .data(caseDataBuilder.build().toMap(
                objectMapper)).build();
    }
}

