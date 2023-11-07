package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.ResponseRepaymentDetailsForm;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.time.LocalDate;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;

@Service
@RequiredArgsConstructor
public class SealedClaimLipResponseFormGenerator implements TemplateDataGenerator<SealedClaimLipResponseForm> {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DeadlineExtensionCalculatorService deadlineCalculatorService;

    @Override
    public SealedClaimLipResponseForm getTemplateData(CaseData caseData) {
        return SealedClaimLipResponseForm.toTemplate(caseData);
    }

    public CaseDocument generate(final CaseData caseData, final String authorization) {
        SealedClaimLipResponseForm templateData = getTemplateData(caseData);
        templateData = getRespondent1RepaymentDate(caseData, templateData);
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
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.DEFENDANT_DEFENCE)
        );
    }

    private SealedClaimLipResponseForm getRespondent1RepaymentDate(final CaseData caseData, SealedClaimLipResponseForm templateData) {
        if (!Objects.isNull(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            if (FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()) && caseData.isPayImmediately()) {
                return addPayByDatePayImmediately(caseData, templateData);
            }
            if (PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()) && caseData.isPayImmediately()) {
                if (caseData.getSpecDefenceAdmittedRequired() == YesOrNo.NO) {
                    return addPayByDatePayImmediately(caseData, templateData);
                }
            }
        }
        return templateData;
    }

    private SealedClaimLipResponseForm addPayByDatePayImmediately(CaseData caseData, SealedClaimLipResponseForm templateData) {
        LocalDate payDeadlineDate = deadlineCalculatorService.calculateExtendedDeadline(LocalDate.now().plusDays(
            RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY));
        ResponseRepaymentDetailsForm responseRepaymentDetailsForm = templateData.getCommonDetails().toBuilder().payBy(
            payDeadlineDate).build();
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder templateFormData = templateData.toBuilder();
        return templateFormData.commonDetails(responseRepaymentDetailsForm).build();
    }
}
