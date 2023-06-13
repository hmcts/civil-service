package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SendSDOBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private  final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;
    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";

    public void sendSDOToDefendantLIP(CaseData caseData) {
        if (caseData.getSystemGeneratedCaseDocuments() != null && !caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            Optional<Element<CaseDocument>> caseDocument = caseData.getSDODocument();

            if (caseDocument.isPresent()) {
                byte[] letterContent = sealedClaimFormGeneratorForSpec.downloadDocument(caseDocument.get().getValue());
                bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(), caseData.getLegacyCaseReference(), SDO_ORDER_PACK_LETTER_TYPE);
            }
        }
    }
}
