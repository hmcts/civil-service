package uk.gov.hmcts.reform.civil.repositories;

import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface SpecReferenceNumberRepository {

    @SqlQuery("SELECT next_SPEC_legal_rep_reference_number()")
    String getSpecReferenceNumber();

}

