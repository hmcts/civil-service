package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondentonesolicitor",
    "uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.respondenttwosolicitor",
    "uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documentbuilder",
    "uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler.retriever",
})
public class RespondentEvidenceHandlerTestConfiguration {
}
