package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"caseReference", "partyType", "unavailableDateType", "fromDate", "toDate"})
@NoArgsConstructor
public class UnavailableDatesCaseReference extends CaseReference implements ExcelMappable {

    @JsonProperty
    private String partyType;
    @JsonProperty
    private String unavailableDateType;
    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;
    @JsonProperty
    private LocalDate toDate;

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) {
        setCaseReference(asString(rowValues.get("caseReference")));
        setPartyType(asString(rowValues.get("partyType")));
        setUnavailableDateType(asString(rowValues.get("unavailableDateType")));
        setFromDate(asLocalDate(rowValues.get("fromDate")));
        setToDate(asLocalDate(rowValues.get("toDate")));
    }

    private String asString(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private LocalDate asLocalDate(Object value) {
        String date = asString(value);
        return date == null || date.isBlank() ? null : LocalDate.parse(date);
    }
}
