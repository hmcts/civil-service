package uk.gov.hmcts.reform.migration.migration.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.migration.migration.MigrationProperties;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class DataMigrationServiceImpl implements DataMigrationService<Map<String, Object>> {

    private static final String MIGRATION_ID_KEY = "migrationId";

    private final MigrationProperties migrationProperties;

    @Override
    public Predicate<CaseDetails> accepts() {
        return caseDetails -> Optional.ofNullable(caseDetails)
            .filter(caseAlreadyProcessed())
            .isPresent();
    }

    private Predicate<CaseDetails> caseAlreadyProcessed() {

        return caseDetails -> !caseDetails.getData().containsKey(MIGRATION_ID_KEY)
            || !caseDetails.getData().getOrDefault(MIGRATION_ID_KEY, "").equals(migrationProperties.getId());
    }

    @Override
    public Map<String, Object> migrate(CaseDetails caseDetails) {
        /*
         Populate a map here with data that wants to be present when connecting with the callback service.
        */
        return Map.of(MIGRATION_ID_KEY, migrationProperties.getId());
    }
}
