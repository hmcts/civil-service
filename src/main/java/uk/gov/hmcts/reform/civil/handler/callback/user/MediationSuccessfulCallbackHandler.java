package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.io.stream.ByteArrayStreamInput;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sendgrid.EmailAttachment;
import uk.gov.hmcts.reform.civil.sendgrid.EmailData;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_STAYED;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;

@Service
@RequiredArgsConstructor
public class MediationSuccessfulCallbackHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;
    private final SendGridClient sendGridClient;
    private static final List<CaseEvent> EVENTS = Collections.singletonList(MEDIATION_SUCCESSFUL);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::submitSuccessfulMediation,
            callbackKey(CallbackType.MID), this::sendCVSMediation,
            callbackKey(CallbackType.SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse submitSuccessfulMediation(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData().toBuilder()
            .businessProcess(BusinessProcess.ready(MEDIATION_SUCCESSFUL))
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .state(CASE_STAYED.name())
            .build();
    }

    private CallbackResponse sendCVSMediation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Optional<EmailData> emailData = prepareEmail(caseData);
        emailData.ifPresent(data -> sendGridClient.sendEmail(caseData.getApplicant1().getPartyEmail(), data));
        log.info("Mediation CSV sending");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private Optional<EmailData> prepareEmail(CaseData data) {
//        String toEmail = "smallclaimsmediation@justice.gov.uk";
        String toEmail = "civilmoneyclaimsdemo@gmail.com";
        String subject = "OCMC Mediation Data";
        String filename = "ocmc_mediation_data.csv";
        String content = generateAttachmentContent(data);
        InputStreamSource inputSource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));


        return Optional.of(EmailData.builder()
                               .to(toEmail)
                               .subject(subject)
                               .attachments(List.of(new EmailAttachment(inputSource, "text/csv", filename)))
                               .build());
    }

    private String generateAttachmentContent(CaseData data) {
        StringBuilder content = new StringBuilder();
        content.append(5);
        content.append(data.getLegacyCaseReference());
        content.append(1);
        content.append(data.getTotalClaimAmount());
        content.append(data.getApplicant1().getType());
        content.append(data.getApplicant1().getCompanyName());
        content.append(data.getApplicant1().getPartyName());
        content.append(data.getApplicant1().getPartyPhone());
        content.append(4);
        content.append(5);
        content.append(data.getApplicant1().getPartyEmail());
        content.append(data.getTotalClaimAmount().compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No");

        return content.toString();
    }
}
