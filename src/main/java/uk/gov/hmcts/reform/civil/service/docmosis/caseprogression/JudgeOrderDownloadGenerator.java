package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_AFTER_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CCMC_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CMC_DOCX;

@Slf4j
@Service
public class JudgeOrderDownloadGenerator extends JudgeFinalOrderGenerator implements TemplateDataGenerator<JudgeFinalOrderForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UserService userService;
    private final LocationReferenceDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;
    private final DocumentHearingLocationHelper documentHearingLocationHelper;
    private LocationRefData caseManagementLocationDetails;
    private DocmosisTemplates docmosisTemplate;
    private static final String INTERMEDIATE_NO_BAND_NO_REASON = "This case is allocated to the Intermediate Track and is not allocated a complexity band.";
    private static final String INTERMEDIATE_NO_BAND_WITH_REASON = "This case is allocated to the Intermediate Track and is not allocated a complexity band because %s.";
    private static final String INTERMEDIATE_WITH_BAND_NO_REASON = "This case is allocated to the Intermediate Track and is allocated to complexity band %s.";
    private static final String INTERMEDIATE_WITH_BAND_WITH_REASON = "This case is allocated to the Intermediate Track and is allocated to complexity band %s because %s.";
    private static final String FAST_NO_BAND_NO_REASON = "This case is allocated to the Fast Track and is not allocated a complexity band.";
    private static final String FAST_NO_BAND_WITH_REASON = "This case is allocated to the Fast Track and is not allocated a complexity band because %s.";
    private static final String FAST_WITH_BAND_NO_REASON = "This case is allocated to the Fast Track and is allocated to complexity band %s.";
    private static final String FAST_WITH_BAND_WITH_REASON = "This case is allocated to the Fast Track and is allocated to complexity band %s because %s.";

    public JudgeOrderDownloadGenerator(DocumentManagementService documentManagementService, DocumentGeneratorService documentGeneratorService, UserService userService, LocationReferenceDataService locationRefDataService, FeatureToggleService featureToggleService, DocumentHearingLocationHelper documentHearingLocationHelper) {
        super(documentManagementService, documentGeneratorService, userService, locationRefDataService, featureToggleService, documentHearingLocationHelper);
        this.documentManagementService = documentManagementService;
        this.documentGeneratorService = documentGeneratorService;
        this.userService = userService;
        this.locationRefDataService = locationRefDataService;
        this.featureToggleService = featureToggleService;
        this.documentHearingLocationHelper = documentHearingLocationHelper;
    }

    public CaseDocument generate(CaseData caseData, String authorisation) {
        JudgeFinalOrderForm templateData = getDownloadTemplate(caseData, authorisation);
        DocmosisDocument docmosisDocument =
            documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate, "docx");
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                docmosisTemplate.getDocumentTitle(),
                docmosisDocument.getBytes(),
                DocumentType.JUDGE_FINAL_ORDER
            )
        );
    }

    private JudgeFinalOrderForm getDownloadTemplate(CaseData caseData, String authorisation) {

        switch (caseData.getFinalOrderDownloadTemplateOptions().getValue().getLabel()) {
            case "Blank template to be used after a hearing":
                docmosisTemplate = BLANK_TEMPLATE_AFTER_HEARING_DOCX;
                return getBlankAfterHearing(caseData, authorisation);
            case "Blank template to be used before a hearing/box work":
                docmosisTemplate = BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
                return getBlankBeforeHearing(caseData, authorisation);
            case "Fix a date for CCMC":
                docmosisTemplate = FIX_DATE_CCMC_DOCX;
                return getFixDateCcmc(caseData, authorisation);
            case "Fix a date for CMC":
                docmosisTemplate = FIX_DATE_CMC_DOCX;
                return getFixDateCmc(caseData, authorisation);
            default:
                return null;
        }
    }

    private JudgeFinalOrderForm getBlankAfterHearing(CaseData caseData, String authorisation) {
        var blankAfterHearingBuilder = JudgeFinalOrderForm.builder();

        return blankAfterHearingBuilder.build();
    }

    private JudgeFinalOrderForm getBlankBeforeHearing(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        caseManagementLocationDetails = documentHearingLocationHelper
            .getCaseManagementLocationDetailsNro(caseData, locationRefDataService, authorisation);

        var blankBeforerHearingBuilder = JudgeFinalOrderForm.builder()
            .judgeNameTitle(userDetails.getFullName())
            .courtName(caseManagementLocationDetails.getExternalShortName())
            .caseNumber(caseData.getCcdCaseReference().toString())
            .claimant1Name(caseData.getApplicant1().getPartyName())
            .claimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant1Name(caseData.getRespondent1().getPartyName())
            .defendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .claimantNum(nonNull(caseData.getApplicant2()) ? "Claimant 1" : "Claimant")
            .defendantNum(nonNull(caseData.getRespondent2()) ? "Defendant 1" : "Defendant")
            .claimTrack(caseData.getFinalOrderAllocateToTrack())
            .trackAndComplexityBandText(getTrackAndComplexityText(caseData));

        return blankBeforerHearingBuilder.build();
    }

    private JudgeFinalOrderForm getFixDateCcmc(CaseData caseData, String authorisation) {
        var fixdateCcmcBuilder = JudgeFinalOrderForm.builder();

        return fixdateCcmcBuilder.build();
    }

    private JudgeFinalOrderForm getFixDateCmc(CaseData caseData, String authorisation) {
        var fixdateCmcBuilder = JudgeFinalOrderForm.builder();

        return fixdateCmcBuilder.build();
    }

    private String getTrackAndComplexityText(CaseData caseData) {
        if (nonNull(caseData.getFinalOrderAllocateToTrack())) {
            switch (caseData.getFinalOrderTrackAllocation()) {
                case SMALL_CLAIM:
                    return "This case is allocated to the Small Claims.";
                case FAST_CLAIM:
                    System.out.println("fast claim track");
                    return getFastClaimTrackAndComplexityText(caseData);
                case INTERMEDIATE_CLAIM:
                    return getIntermediateClaimTrackAndComplexityText(caseData);
                case MULTI_CLAIM:
                    return "This case is allocated to the Multi Track.";
                default:
                    return null;
            }
        }
        return null;
    }

    private String getIntermediateClaimTrackAndComplexityText(CaseData caseData) {
        String complexityBand = getComplexityBand(caseData);
        if (caseData.getFinalOrderIntermediateTrackComplexityBand().getAssignComplexityBand().equals(NO)) {
            return nonNull(caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons()) ?
                format(INTERMEDIATE_NO_BAND_WITH_REASON, caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons() ) :
                INTERMEDIATE_NO_BAND_NO_REASON;
        }
        else return  nonNull(caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons()) ?
            format(INTERMEDIATE_WITH_BAND_WITH_REASON, complexityBand, caseData.getFinalOrderIntermediateTrackComplexityBand().getReasons() ) :
            format(INTERMEDIATE_WITH_BAND_NO_REASON, complexityBand);
    }

    private String getFastClaimTrackAndComplexityText(CaseData caseData) {
        String complexityBand = getComplexityBand(caseData);
        if (caseData.getFinalOrderFastTrackComplexityBand().getAssignComplexityBand().equals(NO)) {
            return nonNull(caseData.getFinalOrderFastTrackComplexityBand().getReasons()) ?
                format(FAST_NO_BAND_WITH_REASON, caseData.getFinalOrderFastTrackComplexityBand().getReasons() ) :
                FAST_NO_BAND_NO_REASON;
        }
        else return  nonNull(caseData.getFinalOrderFastTrackComplexityBand().getReasons()) ?
            format(FAST_WITH_BAND_WITH_REASON, complexityBand, caseData.getFinalOrderFastTrackComplexityBand().getReasons() ) :
            format(FAST_WITH_BAND_NO_REASON, complexityBand);
    }

    private String getComplexityBand(CaseData caseData) {
        if (nonNull(caseData.getFinalOrderIntermediateTrackComplexityBand())
            && caseData.getFinalOrderIntermediateTrackComplexityBand().getAssignComplexityBand().equals(YES)) {
            switch (caseData.getFinalOrderIntermediateTrackComplexityBand().getBand()) {
                case BAND_1:
                    return "1";
                case BAND_2:
                    return "2";
                case BAND_3:
                    return "3";
                case BAND_4:
                    return "4";
                default:
                    return null;
            }
        }
        if (nonNull(caseData.getFinalOrderFastTrackComplexityBand())
            && caseData.getFinalOrderFastTrackComplexityBand().getAssignComplexityBand().equals(YES)) {
            System.out.println("get fast band");
            switch (caseData.getFinalOrderFastTrackComplexityBand().getBand()) {
                case BAND_1:
                    return "1";
                case BAND_2:
                    return "2";
                case BAND_3:
                    return "3";
                case BAND_4:
                    return "4";
                default:
                    return null;
            }
        }
        return null;
    }

}
