package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.properties.mediation.MediationCSVEmailConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer.MediationFileTransferResult.FailedCase;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCSVService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCase;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCases;
import uk.gov.hmcts.reform.civil.service.mediation.MediationDTO;
import uk.gov.hmcts.reform.civil.service.mediation.MediationCsvServiceFactory;
import uk.gov.hmcts.reform.civil.service.mediation.MediationJsonService;
import uk.gov.hmcts.reform.civil.service.search.MediationCasesSearchService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.List.of;
import static uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment.json;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediationFileTransferScheduledTask {

    private static final String SUBJECT = "OCMC Mediation Data";
    private static final String CSV_FILENAME = "ocmc_mediation_data.csv";
    private static final String JSON_FILENAME = "ocmc_mediation_data.json";

    private final MediationCasesSearchService caseSearchService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final SendGridClient sendGridClient;
    private final MediationCSVEmailConfiguration mediationCSVEmailConfiguration;
    private final MediationCsvServiceFactory mediationCsvServiceFactory;
    private final MediationJsonService mediationJsonService;

    public MediationFileTransferResult generateCsvAndTransfer(int circuitBreakerThreshold) {
        List<CaseData> cases = getCases(false);
        logCaseIds("CSV", cases);
        try {
            if (!cases.isEmpty()) {
                String[] headers = getCSVHeaders();
                StringBuilder csvColContent = new StringBuilder();
                cases.forEach(caseData -> csvColContent.append(generateCsvContent(caseData)));

                String generateCsvData = generateCSVRow(headers) + csvColContent;
                prepareCsvEmail(generateCsvData).ifPresent(this::sendMediationFileEmail);
            }
        } catch (Exception e) {
            return failedResult(cases, e);
        }

        return markCasesSent(cases, circuitBreakerThreshold);
    }

    public MediationFileTransferResult generateJsonAndTransfer(int circuitBreakerThreshold) {
        List<CaseData> cases = getCases(true);
        logCaseIds("JSON", cases);
        try {
            if (!cases.isEmpty()) {
                List<MediationCase> casesList = new ArrayList<>();
                for (CaseData caseData : cases) {
                    casesList.add(mediationJsonService.generateJsonContent(caseData));
                }

                prepareJsonEmail(convertToMediationDTO(casesList)).ifPresent(this::sendMediationFileEmail);
            }
        } catch (Exception e) {
            return failedResult(cases, e);
        }

        return markCasesSent(cases, circuitBreakerThreshold);
    }

    private List<CaseData> getCases(boolean carmEnabled) {
        return caseSearchService.getInMediationCases(carmEnabled).stream()
            .map(caseDetailsConverter::toCaseData)
            .toList();
    }

    private void logCaseIds(String fileType, List<CaseData> cases) {
        if (!cases.isEmpty()) {
            log.info("{} mediation file transfer case IDs: {}", fileType, cases.stream()
                .map(caseData -> String.valueOf(caseData.getCcdCaseReference()))
                .toList());
        }
    }

    private MediationFileTransferResult markCasesSent(List<CaseData> cases, int circuitBreakerThreshold) {
        List<String> caseIds = cases.stream()
            .map(caseData -> String.valueOf(caseData.getCcdCaseReference()))
            .toList();
        List<String> succeededCaseIds = new ArrayList<>();
        List<FailedCase> failedCases = new ArrayList<>();
        int consecutiveFailures = 0;
        boolean abortedEarly = false;
        String abortReason = null;

        for (CaseData caseData : cases) {
            String caseId = String.valueOf(caseData.getCcdCaseReference());
            try {
                setMediationFileSent(caseData);
                succeededCaseIds.add(caseId);
                consecutiveFailures = 0;
            } catch (Exception e) {
                failedCases.add(new FailedCase(caseId, e));
                consecutiveFailures++;
                if (consecutiveFailures >= circuitBreakerThreshold) {
                    abortedEarly = true;
                    abortReason = e.getMessage();
                    break;
                }
            }
        }

        return new MediationFileTransferResult(caseIds, succeededCaseIds, failedCases, abortedEarly, abortReason);
    }

    private MediationFileTransferResult failedResult(List<CaseData> cases, Exception exception) {
        List<String> caseIds = cases.stream()
            .map(caseData -> String.valueOf(caseData.getCcdCaseReference()))
            .toList();
        List<FailedCase> failedCases = caseIds.stream()
            .map(caseId -> new FailedCase(caseId, exception))
            .toList();

        return new MediationFileTransferResult(caseIds, List.of(), failedCases, false, null);
    }

    private void setMediationFileSent(CaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        String eventSummary = "Updating case - Mediation File sent to MMT successfully";
        String eventDescription = "Updating case - Mediation File sent to MMT successfully";

        Map<String, Object> newCaseData = new HashMap<>();
        newCaseData.put("mediationFileSentToMmt", YesOrNo.YES);

        coreCaseDataService.triggerEvent(
            caseId,
            CaseEvent.UPDATE_CASE_DATA,
            newCaseData,
            eventSummary,
            eventDescription
        );
    }

    private void sendMediationFileEmail(EmailData data) {
        sendGridClient.sendEmail(
            mediationCSVEmailConfiguration.getSender(),
            data
        );
    }

    private Optional<EmailData> prepareCsvEmail(String generateCsvData) {
        InputStreamSource inputSource = new ByteArrayResource(generateCsvData.getBytes(StandardCharsets.UTF_8));

        return Optional.of(new EmailData()
                               .setTo(mediationCSVEmailConfiguration.getRecipient())
                               .setSubject(SUBJECT)
                               .setAttachments(List.of(new EmailAttachment(inputSource, "text/csv", CSV_FILENAME))));
    }

    private Optional<EmailData> prepareJsonEmail(MediationDTO mediationDTO) {
        return Optional.of(new EmailData()
                               .setTo(mediationCSVEmailConfiguration.getJsonRecipient())
                               .setSubject(SUBJECT)
                               .setAttachments(of(json(mediationDTO.getJsonRawData(), JSON_FILENAME))));
    }

    private String generateCsvContent(CaseData caseData) {
        MediationCSVService mediationCSVService = mediationCsvServiceFactory.getMediationCSVService(caseData);
        return mediationCSVService.generateCSVContent(caseData);
    }

    private String generateCSVRow(String[] row) {
        StringBuilder builder = new StringBuilder();

        for (String s : row) {
            builder.append(s).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("\r\n");

        return builder.toString();
    }

    private String[] getCSVHeaders() {
        return new String[]{"SITE_ID", "CASE_TYPE", "CHECK_LIST", "PARTY_STATUS", "CASE_NUMBER", "AMOUNT",
            "PARTY_TYPE", "COMPANY_NAME", "CONTACT_NAME", "CONTACT_NUMBER", "CONTACT_EMAIL", "PILOT",
            "CASE_TITLE"};
    }

    private MediationDTO convertToMediationDTO(List<MediationCase> list) throws JsonProcessingException {
        MediationCases cases = new MediationCases(list);
        return new MediationDTO(cases.toJsonString().getBytes());
    }
}
