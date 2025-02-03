package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingNoticeHmc;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.civil.utils.HearingUtils;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingDaysText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingTypeContentText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingTypeTitleText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getInPersonAttendeeNames;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getLocationRefData;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getPhoneAttendeeNames;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getTotalHearingDurationText;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getVideoAttendeesNames;

@Service
@RequiredArgsConstructor
public class HearingNoticeHmcGenerator implements TemplateDataGenerator<HearingNoticeHmc> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final LocationReferenceDataService locationRefDataService;
    private final HearingFeesService hearingFeesService;
    private final AssignCategoryId assignCategoryId;
    private final FeatureToggleService featureToggleService;

    public List<CaseDocument> generate(CaseData caseData, HearingGetResponse hearing, String authorisation, String hearingLocation, String hearingId) {

        List<CaseDocument> caseDocuments = new ArrayList<>();
        HearingNoticeHmc templateData = getHearingNoticeTemplateData(caseData, hearing, authorisation, hearingLocation, hearingId);
        DocmosisTemplates template = getTemplate();
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        PDF pdf =  new PDF(
            getFileName(caseData, template),
            document.getBytes(),
            DocumentType.HEARING_FORM
        );
        CaseDocument caseDocument = documentManagementService.uploadDocument(authorisation, pdf);
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.HEARING_NOTICES.getValue());
        caseDocuments.add(caseDocument);
        return caseDocuments;
    }

    public HearingNoticeHmc getHearingNoticeTemplateData(CaseData caseData, HearingGetResponse hearing, String bearerToken,
                                                         String hearingLocation, String hearingId) {
        var paymentFailed = (caseData.getHearingFeePaymentDetails() == null
            || caseData.getHearingFeePaymentDetails().getStatus().equals(PaymentStatus.FAILED))
            && (!featureToggleService.isCaseProgressionEnabled() || !caseData.hearingFeePaymentDoneWithHWF());
        var hearingType = hearing.getHearingDetails().getHearingType();
        var feeAmount = paymentFailed && hearingFeeRequired(hearingType)
            ? HearingUtils.formatHearingFee(HearingFeeUtils.calculateAndApplyFee(hearingFeesService, caseData, caseData.getAssignedTrack())) : null;
        var hearingDueDate = paymentFailed && hearingFeeRequired(hearingType) ? HearingFeeUtils
            .calculateHearingDueDate(LocalDate.now(), HmcDataUtils.getHearingStartDay(hearing)
                .getHearingStartDateTime().toLocalDate()) : null;

        LocationRefData caseManagementLocation =
            getLocationRefData(hearingId, caseData.getCaseManagementLocation().getBaseLocation(), bearerToken, locationRefDataService);

        return HearingNoticeHmc.builder()
            .title(getHearingTypeTitleText(caseData, hearing))
            .hearingSiteName(nonNull(caseManagementLocation) ? caseManagementLocation.getExternalShortName() : null)
            .caseManagementLocation(nonNull(caseManagementLocation) ? LocationReferenceDataService.getDisplayEntry(caseManagementLocation) : null)
            .hearingLocation(hearingLocation)
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .hearingType(getHearingTypeContentText(caseData, hearing))
            .claimant(caseData.getApplicant1().getPartyName())
            .claimantReference(nonNull(caseData.getSolicitorReferences())
                                   ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .claimant2(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .claimant2Reference(nonNull(caseData.getApplicant2())
                    && nonNull(caseData.getSolicitorReferences()) ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendant(caseData.getRespondent1().getPartyName())
            .defendantReference(nonNull(caseData.getSolicitorReferences())
                                    ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
            .defendant2(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .defendant2Reference(caseData.getRespondentSolicitor2Reference())
            .hearingDays(getHearingDaysText(hearing))
            .totalHearingDuration(getTotalHearingDurationText(hearing))
            .feeAmount(feeAmount)
            .hearingDueDate(hearingDueDate)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .partiesAttendingInPerson(getInPersonAttendeeNames(hearing))
            .partiesAttendingByTelephone(getPhoneAttendeeNames(hearing))
            .partiesAttendingByVideo(getVideoAttendeesNames(hearing))
            .build();
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template) {
        return String.format(template.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private DocmosisTemplates getTemplate() {
        return HEARING_NOTICE_HMC;
    }
}

