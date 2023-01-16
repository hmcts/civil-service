package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocTypeOtherDocumentsEnum {

    @JsonProperty("applicantStatement")
    applicantStatement(
        "applicantStatement",
        "Applicant statement - for example photographic evidence, witness statement, mobile phone screenshot"),
    @JsonProperty("cafcassReports")
    cafcassReports("cafcassReports", "Cafcass reports"),
    @JsonProperty("expertReports")
    expertReports("expertReports", "Expert reports"),
    @JsonProperty("respondentReports")
    respondentReports("respondentReports", "Respondent reports"),
    @JsonProperty("otherReports")
    otherReports("otherReports", "Other reports");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DocTypeOtherDocumentsEnum getValue(String key) {
        return DocTypeOtherDocumentsEnum.valueOf(key);
    }
}
