package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;

@Service
@RequiredArgsConstructor
public class SealedClaimLipResponseFormGenerator implements TemplateDataGenerator<SealedClaimLipResponseForm> {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;

    @Override
    public SealedClaimLipResponseForm getTemplateData(CaseData caseData) {
        var builder = SealedClaimLipResponseForm.builder()
            .respondent1(caseData.getRespondent1())
            .referenceNumber(caseData.getLegacyCaseReference())
            .caseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .whyDisputeTheClaim(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .statementOfTruth(caseData.getRespondent1DQ().getRespondent1DQStatementOfTruth());

        Stream.of(caseData.getRespondToClaim(), caseData.getRespondToAdmittedClaim())
            .filter(Objects::nonNull)
            .findFirst()
            .ifPresent(response -> builder.poundsPaid(MonetaryConversions
                                                          .penniesToPounds(response.getHowMuchWasPaid()).toString())
                .paymentDate(response.getWhenWasThisAmountPaid()));


        return builder.build();
    }

    public CaseDocument generate(CaseData caseData, String authorization) {
        SealedClaimLipResponseForm templateData = getTemplateData(caseData);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
            templateData,
            DEFENDANT_RESPONSE_LIP_SPEC
        );
        String fileName = String.format(
            DEFENDANT_RESPONSE_LIP_SPEC.getDocumentTitle(),
            caseData.getLegacyCaseReference()
        );

        return documentManagementService.uploadDocument(
            authorization,
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.SEALED_CLAIM)
        );
    }
}
