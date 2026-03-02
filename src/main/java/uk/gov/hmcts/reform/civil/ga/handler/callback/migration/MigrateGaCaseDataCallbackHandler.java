package uk.gov.hmcts.reform.civil.ga.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.migrateCase;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrateGaCaseDataCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(migrateCase);

    private static final String MIGRATION_ID_VALUE = "GAMigration";
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::migrateCaseData
        );
    }

    private CallbackResponse migrateCaseData(CallbackParams callbackParams) {

        GeneralApplicationCaseData oldCaseData = callbackParams.getGeneralApplicationCaseData();
        log.info("Migrating data for case: {}", oldCaseData.getCcdCaseReference());
        GeneralApplicationCaseData caseDataBuilder = oldCaseData.copy();
        caseDataBuilder.migrationId(MIGRATION_ID_VALUE);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
