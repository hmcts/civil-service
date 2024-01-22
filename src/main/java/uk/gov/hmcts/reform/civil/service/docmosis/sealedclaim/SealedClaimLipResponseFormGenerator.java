package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;

@Service
@RequiredArgsConstructor
public class SealedClaimLipResponseFormGenerator implements TemplateDataGenerator<SealedClaimLipResponseForm> {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final FeatureToggleService featureToggleService;

    @Override
    public SealedClaimLipResponseForm getTemplateData(CaseData caseData) {
        SealedClaimLipResponseForm.toTemplate(caseData);
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder responseFormBuilder =
            SealedClaimLipResponseForm.toTemplate(caseData).toBuilder()
                .checkCarmToggle(featureToggleService.isCarmEnabledForCase(caseData.getSubmittedDate()))
                .defendant1MediationCompanyName(getDefendant1MediationCompanyName(caseData))
                .defendant1MediationContactNumber(getDefendant1MediationContactNumber(caseData))
                .defendant1MediationEmail(getDefendant1MediationEmail(caseData))
                .defendant1MediationUnavailableDatesExists(checkDefendant1MediationHasUnavailabilityDates(caseData))
                .defendant1UnavailableDateFromForMediation(getDefendant1MediationUnavailableDate(caseData, YesOrNo.YES))
                .defendant1UnavailableDateToForMediation(getDefendant1MediationUnavailableDate(caseData, YesOrNo.NO));
        return responseFormBuilder.build();
    }

    private LocalDate getDefendant1MediationUnavailableDate(CaseData caseData, YesOrNo unavailabilityFrom) {
        LocalDate defendant1MediationDateFrom = null;
        LocalDate defendant1MediationDateTo = null;
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse()
            .getHasUnavailabilityNextThreeMonths().equals(YesOrNo.YES)) {

            List<Element<UnavailableDate>> datesUnavailableList = caseData.getCaseDataLiP().getRespondent1LiPResponse()
                .getRespondent1MediationLiPResponse().getUnavailableDatesForMediation();
            for (Element<UnavailableDate> dateRange : datesUnavailableList) {
                defendant1MediationDateFrom = dateRange.getValue().getFromDate();
                defendant1MediationDateTo = dateRange.getValue().getUnavailableDateType().equals(UnavailableDateType.SINGLE_DATE)
                    ? dateRange.getValue().getDate() : dateRange.getValue().getToDate();
            }
        }
        return unavailabilityFrom == YesOrNo.YES ? defendant1MediationDateFrom : defendant1MediationDateTo;
    }

    private boolean checkDefendant1MediationHasUnavailabilityDates(CaseData caseData) {
        return caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse().getHasUnavailabilityNextThreeMonths().equals(
            YesOrNo.YES);
    }

    private String getDefendant1MediationEmail(CaseData caseData) {
        String mediationEmail = caseData.getRespondent1().getPartyEmail();
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse().getIsMediationEmailCorrect().equals(YesOrNo.NO)) {
            mediationEmail = caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse().getAlternativeMediationEmail();
        }
        return mediationEmail;
    }

    private String getDefendant1MediationContactNumber(CaseData caseData) {
        String mediationContactNumber = caseData.getRespondent1().getPartyPhone();
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse().getIsMediationPhoneCorrect().equals(YesOrNo.NO)) {
            mediationContactNumber = caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse().getAlternativeMediationTelephone();
        }
        return mediationContactNumber;
    }

    private String getDefendant1MediationCompanyName(CaseData caseData) {

        String mediationContactName = caseData.getRespondent1().getPartyName();
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse() != null
            && caseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1MediationLiPResponse()
            .getIsMediationContactNameCorrect().equals(YesOrNo.NO)) {
            mediationContactName = caseData.getCaseDataLiP().getRespondent1LiPResponse()
                    .getRespondent1MediationLiPResponse().getAlternativeMediationContactPerson();
        }

        return mediationContactName;
    }

    public CaseDocument generate(final CaseData caseData, final String authorization) {
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
            new PDF(fileName, docmosisDocument.getBytes(), DocumentType.DEFENDANT_DEFENCE)
        );
    }
}
