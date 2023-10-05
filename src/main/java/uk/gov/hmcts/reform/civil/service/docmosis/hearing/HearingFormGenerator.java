package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingForm;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_APPLICATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_FAST_TRACK;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_OTHER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_SMALL_CLAIMS;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.formatHearingDuration;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingTimeFormatted;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingType;

@Service
@RequiredArgsConstructor
public class HearingFormGenerator implements TemplateDataGenerator<HearingForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final AssignCategoryId assignCategoryId;

    public List<CaseDocument> generate(CaseData caseData, String authorisation) {

        List<CaseDocument> caseDocuments = new ArrayList<>();
        HearingForm templateData = getTemplateData(caseData);
        DocmosisTemplates template = getTemplate(caseData);
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        CaseDocument caseDocument = documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                        getFileName(caseData, template),
                        document.getBytes(),
                        DocumentType.HEARING_FORM
                )
        );
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.HEARING_NOTICES.getValue());
        caseDocuments.add(caseDocument);
        return caseDocuments;
    }

    @Override
    public HearingForm getTemplateData(CaseData caseData) {

        return HearingForm.builder()
            .court(caseData.getHearingLocation().getValue().getLabel())
            .caseNumber(caseData.getLegacyCaseReference())
            .creationDate(getDateFormatted(LocalDate.now()))
            .claimant(caseData.getApplicant1().getPartyName())
            .claimantReference(checkReference(caseData)
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendant(caseData.getRespondent1().getPartyName())
            .defendantReference(checkReference(caseData)
                                    ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
            .hearingDate(getDateFormatted(caseData.getHearingDate()))
            .hearingTime(getHearingTimeFormatted(caseData.getHearingTimeHourMinute()))
            .hearingType(getHearingType(caseData))
            .applicationDate(getDateFormatted(caseData.getDateOfApplication()))
            .hearingDuration(formatHearingDuration(caseData.getHearingDuration()))
            .additionalInfo(caseData.getInformation())
            .feeAmount(HearingUtils.formatHearingFee(caseData.getHearingFee()))
            .hearingDueDate(getDateFormatted(caseData.getHearingDueDate()))
            .additionalText(caseData.getHearingNoticeListOther())
            .claimant2exists(nonNull(caseData.getApplicant2()))
            .defendant2exists(nonNull(caseData.getRespondent2()))
            .claimant2(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant2(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .claimant2Reference(checkReference(caseData)
                                    ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendant2Reference(checkReference(caseData)
                                     ? caseData.getSolicitorReferences().getRespondentSolicitor2Reference() : null)
            .build();
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template) {
        return String.format(template.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private String getDateFormatted(LocalDate date) {
        if (isNull(date)) {
            return null;
        }
        return fixSepDateIssue(DateFormatHelper.formatLocalDate(date, "dd/MMM/yyyy"));
    }

    private String fixSepDateIssue(String fixDate) {
        return  fixDate.contains("Sept") ? fixDate.replace("Sept", "Sep") : fixDate;

    }

    private boolean checkReference(CaseData caseData) {
        return nonNull(caseData.getSolicitorReferences());
    }

    private DocmosisTemplates getTemplate(CaseData caseData) {
        switch (caseData.getHearingNoticeList()) {
            case SMALL_CLAIMS:
                return HEARING_SMALL_CLAIMS;
            case FAST_TRACK_TRIAL:
                return HEARING_FAST_TRACK;
            case HEARING_OF_APPLICATION:
                return HEARING_APPLICATION;
            default:
                return HEARING_OTHER;
        }
    }
}

