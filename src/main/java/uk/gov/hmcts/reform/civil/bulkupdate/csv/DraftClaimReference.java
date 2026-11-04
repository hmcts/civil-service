package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class DraftClaimReference extends CaseReference implements ExcelMappable {

    private String userId;

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) {
        setCaseReference(value(rowValues, "caseReference"));
        userId = value(rowValues, "userId");
    }

    private String value(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null ? null : value.toString();
    }
}
