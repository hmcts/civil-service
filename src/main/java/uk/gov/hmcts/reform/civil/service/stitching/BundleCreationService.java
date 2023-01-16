package uk.gov.hmcts.reform.civil.service.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.CaseDetails;
import uk.gov.hmcts.reform.civil.CaseDefinitionConstants;
import uk.gov.hmcts.reform.civil.config.StitchingConfiguration;
import uk.gov.hmcts.reform.civil.event.BundleCreationTriggerEvent;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.BundleCreateRequestMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.documentmanagement.UnsecuredDocumentManagementService.CREATED_BY;

public class BundleCreationService {

    @Autowired
    private BundleCreationTriggerEvent bundleCreationTriggerEvent;
    private BundleRequestExecutor bundleRequestExecutor;
    private ObjectMapper objectMapper;
    @Autowired
    private BundleCreateRequestMapper bundleCreateRequestMapper;
    private StitchingConfiguration stitchingConfiguration;

    public CaseDocument CreateBundleDoucment(
        BundleCreationTriggerEvent bundleCreationTriggerEvent
    ) {
            CaseDocument caseDocument = null;
            CaseDetails payload =
                createBundlePayload(
                    bundleCreationTriggerEvent
                );
            log.info("Calling api end point for {}", bundleCreationTriggerEvent.getCaseId());
            CaseData caseData1 =
                bundleRequestExecutor.post(
                    BundleRequest.builder().caseDetails(payload).build(),
                    stitchingConfiguration.getStitchingUrl()
                );
            log.info("Called api end point for {}", bundleCreationTriggerEvent.getCaseId());
            if (caseData1 != null) {
                Optional<Document> stitchedDocument = caseData1.getCaseBundles().get(0).getValue().getStitchedDocument();

                log.info("stitchedDocument.isPresent() {}, legacy case reference {}",  stitchedDocument.isPresent(),
                         bundleCreationTriggerEvent.getCaseId());
                if (stitchedDocument.isPresent()) {
                    Document document = stitchedDocument.get();
                    String documentUrl = document.getDocumentUrl();
                    String documentBinaryUrl = document.getDocumentBinaryUrl();
                    caseDocument = CaseDocument.builder()
                        .documentLink(Document.builder()
                                          .documentUrl(documentUrl)
                                          .documentBinaryUrl(documentBinaryUrl)
                                          .documentFileName(document.getDocumentFileName())
                                          .build())
                        .documentName("Stitched document")
                        .documentType(SEALED_CLAIM)
                        .createdDatetime(LocalDateTime.now())
                        .createdBy(CREATED_BY)
                        .build();
                } else {
                    log.info("stitchedDocument is not present----------");
                }
            } else {
                log.info("Case data is null----------");
            }

            return caseDocument;
        }
        private CaseDetails createBundlePayload (
            BundleCreationTriggerEvent bundleCreationTriggerEvent
    ) {

            List<IdValue<BundleDocument>> bundleDocuments = new ArrayList<>();
            List<Value<Document>> caseDocuments = new ArrayList<>();

            for (int i = 0; i < documents.size(); i++) {

                DocumentMetaData caseDocument = documents.get(i);
                caseDocuments.add(
                    new Value<>(caseDocument.getDocument().getDocumentFileName(),
                                caseDocument.getDocument()));

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
            caseDataBuilder.caseDocuments(caseDocuments);
            caseDataBuilder.caseDocument1Name(bundleFilename);

            Map<String, Object> data = Map.of("case_details", caseDataBuilder.build().toMap(
                objectMapper));

            return CaseDetails.builder().id(caseData.getCcdCaseReference())
                .data(caseDataBuilder.build().toMap(
                    objectMapper))
                .caseTypeId(CaseDefinitionConstants.CASE_TYPE)
                .jurisdictionId(CaseDefinitionConstants.JURISDICTION).build();
        }


}
