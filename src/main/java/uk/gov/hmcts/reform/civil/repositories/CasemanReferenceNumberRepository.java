package uk.gov.hmcts.reform.civil.repositories;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface CasemanReferenceNumberRepository {

    @SqlQuery("SELECT next_caseman_reference(:series)")
    String next(@Bind("series") String series);

}
