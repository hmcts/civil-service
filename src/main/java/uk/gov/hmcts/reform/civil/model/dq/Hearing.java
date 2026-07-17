package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Hearing {

    @CCD(label = "How long do you estimate the hearing will take?", searchable = false)
    private HearingLength hearingLength;
    @CCD(
            label = "How many hours?",
            hint = "Enter a numeric value less than 7, for example 3",
            showCondition = "hearingLength = \"LESS_THAN_DAY\"",
            searchable = false,
            min = 1,
            max = 6
    )
    private String hearingLengthHours;
    @CCD(
            label = "How many days?",
            hint = "Enter a numeric value less than 29, for example 10",
            showCondition = "hearingLength = \"MORE_THAN_DAY\"",
            searchable = false,
            min = 1,
            max = 28
    )
    private String hearingLengthDays;
    @CCD(
            label = "Are there any days in the next 12 months when your client, an expert, or a witness, cannot attend a hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo unavailableDatesRequired;
    @CCD(label = "Unavailable date", showCondition = "unavailableDatesRequired = \"Yes\"", searchable = false)
    private List<Element<UnavailableDate>> unavailableDates;

    public Hearing copy() {
        return new Hearing()
            .setHearingLength(hearingLength)
            .setHearingLengthHours(hearingLengthHours)
            .setHearingLengthDays(hearingLengthDays)
            .setUnavailableDatesRequired(unavailableDatesRequired)
            .setUnavailableDates(unavailableDates);
    }

}
