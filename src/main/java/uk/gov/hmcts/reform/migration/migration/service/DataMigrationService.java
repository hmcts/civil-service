package uk.gov.hmcts.reform.migration.migration.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.function.Predicate;

public interface DataMigrationService<T> {
    Predicate<CaseDetails> accepts();

    T migrate(CaseDetails caseDetails);
}
