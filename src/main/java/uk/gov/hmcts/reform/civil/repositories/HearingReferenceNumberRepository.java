package uk.gov.hmcts.reform.civil.repositories;

import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface HearingReferenceNumberRepository {

    @SqlQuery("SELECT next_hearing_reference_number()")
    String getHearingReferenceNumber();

}

