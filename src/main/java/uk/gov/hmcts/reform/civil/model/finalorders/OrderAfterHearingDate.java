package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAfterHearingDate {

    @CCD(label = "Enter date(s) of hearing", searchable = false)
    private OrderAfterHearingDateType dateType;
    @CCD(
            label = "Date",
            hint = "For example 16 4 2021",
            showCondition = "dateType = \"SINGLE_DATE\"",
            searchable = false
    )
    private LocalDate date;
    @CCD(
            label = "Date from",
            hint = "For example 16 4 2021",
            showCondition = "dateType = \"DATE_RANGE\"",
            searchable = false
    )
    private LocalDate fromDate;
    @CCD(
            label = "Date to",
            hint = "For example 16 4 2021",
            showCondition = "dateType = \"DATE_RANGE\"",
            searchable = false
    )
    private LocalDate toDate;
    @CCD(
            label = "Enter a bespoke range of dates for hearing",
            hint = "For example, 16 4 2021, 19-20 4 2021 and 11-12 5 2021",
            showCondition = "dateType = \"BESPOKE_RANGE\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String bespokeDates;
}
