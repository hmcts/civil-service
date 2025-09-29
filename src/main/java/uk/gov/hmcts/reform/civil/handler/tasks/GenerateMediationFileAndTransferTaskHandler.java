package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.MediationCasesSearchService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@Slf4j
public abstract class GenerateMediationFileAndTransferTaskHandler extends BaseExternalTaskHandler {

    protected final MediationCasesSearchService caseSearchService;
    protected final CoreCaseDataService coreCaseDataService;
    protected final CaseDetailsConverter caseDetailsConverter;
    protected final SendGridClient sendGridClient;
    protected final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    protected static final String SUBJECT = "OCMC Mediation Data";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    protected GenerateMediationFileAndTransferTaskHandler(MediationCasesSearchService caseSearchService,
                                                          CoreCaseDataService coreCaseDataService,
                                                          CaseDetailsConverter caseDetailsConverter,
                                                          SendGridClient sendGridClient,
                                                          MediationCSVEmailConfiguration mediationCSVEmailConfiguration) {
        this.caseSearchService = caseSearchService;
        this.coreCaseDataService = coreCaseDataService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.sendGridClient = sendGridClient;
        this.mediationCSVEmailConfiguration = mediationCSVEmailConfiguration;
    }

    protected void setMediationFileSent(CaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        String eventSummary = "Updating case - Mediation File sent to MMT successfully";
        String eventDescription = "Updating case - Mediation File sent to MMT successfully";

        Map<String, Object> newCaseData = new HashMap<>();
        newCaseData.put("mediationFileSentToMmt", YesOrNo.YES);

        coreCaseDataService.triggerEvent(
            caseId,
            UPDATE_CASE_DATA,
            newCaseData,
            eventSummary,
            eventDescription
        );
    }

    protected void sendMediationFileEmail(EmailData data) {
        sendGridClient.sendEmail(
            mediationCSVEmailConfiguration.getSender(),
            data
        );
    }
}
