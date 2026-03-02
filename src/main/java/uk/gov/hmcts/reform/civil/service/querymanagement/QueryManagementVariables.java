package uk.gov.hmcts.reform.civil.service.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class QueryManagementVariables implements MappableObject {

    public String queryId;
    public String documentToRemoveId;
    public boolean removeDocument;
}
