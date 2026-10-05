package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UpdateTTLCaseReference extends ExcelCaseReference {

    private String systemTTL;

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        super.fromExcelRow(rowValues);
        if (rowValues.containsKey("ttl")) {
            Object value = rowValues.get("ttl");
            setSystemTTL(value != null ? value.toString() : null);
        }
    }
}
