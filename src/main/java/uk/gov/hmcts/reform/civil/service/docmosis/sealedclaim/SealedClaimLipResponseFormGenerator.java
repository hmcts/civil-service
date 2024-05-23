package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimLipResponseForm;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_SPEC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFENDANT_RESPONSE_LIP_STITCH_SPEC;

@Service
@RequiredArgsConstructor
public class SealedClaimLipResponseFormGenerator implements TemplateDataGenerator<SealedClaimLipResponseForm> {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final FeatureToggleService featureToggleService;

    @Override
    public SealedClaimLipResponseForm getTemplateData(CaseData caseData) {
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            SealedClaimLipResponseForm.toTemplate(caseData);
            SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder responseFormBuilder =
                SealedClaimLipResponseForm.toTemplate(caseData).toBuilder()
                    .checkCarmToggle(featureToggleService.isCarmEnabledForCase(caseData))
                    .defendant1MediationCompanyName(getDefendant1MediationCompanyName(caseData))
                    .defendant1MediationContactNumber(getDefendant1MediationContactNumber(caseData))
                    .defendant1MediationEmail(getDefendant1MediationEmail(caseData))
                    .defendant1MediationUnavailableDatesExists(checkDefendant1MediationHasUnavailabilityDates(caseData))
                    .defendant1UnavailableDatesList(getDefendant1FromDateUnavailableList(caseData));
            return responseFormBuilder.build();

        } else {
            return  SealedClaimLipResponseForm.toTemplate(caseData);
        }
    }

    private List<Element<UnavailableDate>> getDefendant1FromDateUnavailableList(CaseData caseData) {
        List<Element<UnavailableDate>> datesUnavailableList = null;
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getHasUnavailabilityNextThreeMonths() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm()
            .getHasUnavailabilityNextThreeMonths().equals(YesOrNo.YES)) {

            datesUnavailableList = caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm()
                .getUnavailableDatesForMediation();

        }
        return datesUnavailableList;
    }

    private boolean checkDefendant1MediationHasUnavailabilityDates(CaseData caseData) {
        return caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getHasUnavailabilityNextThreeMonths() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getHasUnavailabilityNextThreeMonths().equals(
            YesOrNo.YES);
    }

    private String getDefendant1MediationEmail(CaseData caseData) {
        String mediationEmail = caseData.getRespondent1().getPartyEmail();
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getIsMediationEmailCorrect() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getIsMediationEmailCorrect().equals(YesOrNo.NO)) {
            mediationEmail = caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getAlternativeMediationEmail();
        }
        return mediationEmail;
    }

    private String getDefendant1MediationContactNumber(CaseData caseData) {
        String mediationContactNumber = caseData.getRespondent1().getPartyPhone();
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getIsMediationPhoneCorrect() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getIsMediationPhoneCorrect().equals(YesOrNo.NO)) {
            mediationContactNumber = caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getAlternativeMediationTelephone();
        }
        return mediationContactNumber;
    }

    private String getDefendant1MediationCompanyName(CaseData caseData) {

        String mediationContactName = caseData.getRespondent1().getPartyName();
        if (caseData.getCaseDataLiP() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm().getIsMediationContactNameCorrect() != null
            && caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm()
            .getIsMediationContactNameCorrect().equals(YesOrNo.NO)) {
            mediationContactName = caseData.getCaseDataLiP().getRespondent1MediationLiPResponseCarm()
                    .getAlternativeMediationContactPerson();
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

    public CaseDocument generateLipResponseDoc(final CaseData caseData, final String authorization) {
        SealedClaimLipResponseForm templateData = getTemplateData(caseData);
        DocmosisDocument docmosisDocument = documentGeneratorService.generateDocmosisDocument(
                templateData,
                DEFENDANT_RESPONSE_LIP_STITCH_SPEC
        );
        String fileName = String.format(
                DEFENDANT_RESPONSE_LIP_STITCH_SPEC.getDocumentTitle(),
                caseData.getLegacyCaseReference()
        );

        return documentManagementService.uploadDocument(
                authorization,
                new PDF(fileName, docmosisDocument.getBytes(), DocumentType.DEFENDANT_RESPONSE)
        );
    }
}
