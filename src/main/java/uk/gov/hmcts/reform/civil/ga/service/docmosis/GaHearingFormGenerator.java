package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.GaHearingForm;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Long.parseLong;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_APPLICATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_HEARING_APPLICATION_LIP;

@Slf4j
@Service
@RequiredArgsConstructor
public class GaHearingFormGenerator implements TemplateDataGenerator<GaHearingForm> {

    private final CaseDetailsConverter caseDetailsConverter;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final GaCoreCaseDataService coreCaseDataService;
    private final DocmosisService docmosisService;

    public CaseDocument generate(GeneralApplicationCaseData caseData, String authorisation) {

        GaHearingForm templateData = getTemplateData(null, caseData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        log.info("Generate hearing form with one respondent representative for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
    }

    public CaseDocument generate(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        GaHearingForm templateData = getTemplateData(civilCaseData, caseData, authorisation, userType);
        log.info("Generate hearing form for caseId: {}", caseData.getCcdCaseReference());
        return generateDocmosisDocument(templateData, authorisation, userType);
    }

    public CaseDocument generateDocmosisDocument(GaHearingForm templateData, String authorisation, FlowFlag userType) {
        DocmosisTemplates template = getTemplate(userType);
        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(template),
                document.getBytes(),
                DocumentType.HEARING_NOTICE
            )
        );
    }

    @Override
    public GaHearingForm getTemplateData(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData caseData, String authorisation, FlowFlag userType) {

        CaseDetails parentCase = coreCaseDataService
                .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference()));
        GeneralApplicationCaseData parentCaseData = caseDetailsConverter.toGeneralApplicationCaseData(parentCase);
        boolean claimant1exists = canViewClaimant(parentCaseData, caseData);
        boolean claimant2exists = claimant1exists && nonNull(caseData.getClaimant2PartyName());
        boolean defendant1exists = canViewResp(parentCaseData, caseData, "1");
        boolean defendant2exists = canViewResp(parentCaseData, caseData, "2")
                && nonNull(caseData.getDefendant2PartyName());

        GaHearingForm hearingForm = new GaHearingForm()
            .setCourt(docmosisService.getCaseManagementLocationVenueName(caseData, authorisation).getExternalShortName())
            .setJudgeHearingLocation(caseData.getGaHearingNoticeDetail().getHearingLocation().getValue().getLabel())
            .setCaseNumber(getCaseNumberFormatted(caseData))
            .setCreationDate(getDateFormatted(LocalDate.now()))
            .setClaimant(caseData.getClaimant1PartyName())
            .setClaimantReference(getReference(parentCase, "applicantSolicitor1Reference"))
            .setDefendant(caseData.getDefendant1PartyName())
            .setDefendantReference(getReference(parentCase, "respondentSolicitor1Reference"))
            .setHearingDate(getDateFormatted(caseData.getGaHearingNoticeDetail().getHearingDate()))
            .setHearingTime(getHearingTimeFormatted(caseData.getGaHearingNoticeDetail().getHearingTimeHourMinute()))
            .setHearingType(caseData.getGaHearingNoticeDetail().getChannel().getDisplayedValue())
            .setApplicationDate(getDateFormatted(caseData.getGaHearingNoticeApplication()
                                                     .getHearingNoticeApplicationDate()))
            .setHearingDuration(getHearingDurationString(caseData))
            .setAdditionalInfo(caseData.getGaHearingNoticeInformation())
            .setApplicant(caseData.getApplicantPartyName())
            .setClaimant1exists(claimant1exists)
            .setDefendant1exists(defendant1exists)
            .setClaimant2exists(claimant2exists)
            .setDefendant2exists(defendant2exists)
            .setClaimant2(nonNull(caseData.getClaimant2PartyName()) ? caseData.getClaimant2PartyName() : null)
            .setDefendant2(nonNull(caseData.getDefendant2PartyName()) ? caseData.getDefendant2PartyName() : null)
            .setClaimant2Reference(getReference(parentCase, "applicantSolicitor1Reference"))
            .setDefendant2Reference(getReference(parentCase, "respondentSolicitor2Reference"));

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            boolean parentClaimantIsApplicant = caseData.identifyParentClaimantIsApplicant(caseData);

            hearingForm
                .setPartyName(caseData.getPartyName(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine1(caseData.partyAddressAddressLine1(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine2(caseData.partyAddressAddressLine2(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressAddressLine3(caseData.partyAddressAddressLine3(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressPostCode(caseData.partyAddressPostCode(parentClaimantIsApplicant, userType, civilCaseData))
                .setPartyAddressPostTown(caseData.partyAddressPostTown(parentClaimantIsApplicant, userType, civilCaseData));
        }

        return hearingForm;
    }

    protected String getCaseNumberFormatted(GeneralApplicationCaseData caseData) {
        String[] parts = caseData.getGeneralAppParentCaseLink().getCaseReference().split("(?<=\\G.{4})");
        return String.join("-", parts);
    }

    protected String getFileName(DocmosisTemplates template) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format(template.getDocumentTitle(),
                LocalDateTime.now().format(formatter));
    }

    protected String getDateFormatted(LocalDate date) {
        if (isNull(date)) {
            return null;
        }
        return DateFormatHelper.formatLocalDate(date, "d MMMM yyyy");
    }

    @SuppressWarnings("unchecked")
    protected String getReference(CaseDetails caseData, String refKey) {
        if (nonNull(caseData.getData().get("solicitorReferences"))) {
            return ((Map<String, String>) caseData.getData().get("solicitorReferences")).get(refKey);
        }
        return null;
    }

    protected static String getHearingTimeFormatted(String hearingTime) {
        if (isEmpty(hearingTime) || hearingTime.length() != 4 || !hearingTime.matches("[0-9]+")) {
            return null;
        }

        StringBuilder hearingTimeBuilder = new StringBuilder(hearingTime);
        hearingTimeBuilder.insert(2, ':');
        return hearingTimeBuilder.toString();
    }

    protected static String getHearingDurationString(GeneralApplicationCaseData caseData) {
        if (caseData.getGaHearingNoticeDetail().getHearingDuration().equals(GAHearingDuration.OTHER)) {
            return caseData.getGaHearingNoticeDetail().getHearingDurationOther();
        }
        return caseData.getGaHearingNoticeDetail().getHearingDuration().getDisplayedValue();
    }

    protected static boolean canViewClaimant(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData generalAppCaseData) {
        List<Element<GeneralApplicationsDetails>> gaAppDetails = civilCaseData.getClaimantGaAppDetails();
        if (Objects.isNull(gaAppDetails)) {
            return false;
        }
        return gaAppDetails.stream()
                .anyMatch(civilGaData -> generalAppCaseData.getCcdCaseReference()
                        .equals(parseLong(civilGaData.getValue().getCaseLink().getCaseReference())));
    }

    protected static boolean canViewResp(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData generalAppCaseData, String respondent) {
        List<Element<GADetailsRespondentSol>> gaAppDetails;
        if (respondent.equals("2")) {
            gaAppDetails = civilCaseData.getRespondentSolTwoGaAppDetails();
        } else {
            gaAppDetails = civilCaseData.getRespondentSolGaAppDetails();
        }
        if (Objects.isNull(gaAppDetails)) {
            return false;
        }
        return gaAppDetails.stream()
                .anyMatch(civilGaData -> generalAppCaseData.getCcdCaseReference()
                        .equals(parseLong(civilGaData.getValue().getCaseLink().getCaseReference())));
    }

    protected DocmosisTemplates getTemplate(FlowFlag userType) {

        if (List.of(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT, FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT).contains(userType)) {
            return POST_JUDGE_HEARING_APPLICATION_LIP;
        }
        return HEARING_APPLICATION;
    }
}
