package uk.gov.hmcts.reform.civil.repositories;

import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface BulkClaimReferenceNumberRepository {

    @SqlQuery("SELECT next_bulk_claims_reference_number()")
    String getReferenceNumber();
}
