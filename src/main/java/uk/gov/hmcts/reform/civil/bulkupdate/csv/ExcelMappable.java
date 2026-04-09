package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import java.util.Map;

public interface ExcelMappable {

    void fromExcelRow(Map<String, Object> rowValues) throws Exception;
}
