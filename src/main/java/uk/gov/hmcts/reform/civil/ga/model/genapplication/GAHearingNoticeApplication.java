package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.ga.enums.hearing.HearingApplicationDetails;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class GAHearingNoticeApplication {

    private HearingApplicationDetails hearingNoticeApplicationDetail;
    private String hearingNoticeApplicationType;
    private LocalDate hearingNoticeApplicationDate;

    @JsonCreator
    GAHearingNoticeApplication(@JsonProperty("hearingNoticeApplicationDetail")
                               HearingApplicationDetails hearingNoticeApplicationDetail,
                               @JsonProperty("hearingNoticeApplicationType") String hearingNoticeApplicationType,
                               @JsonProperty("hearingNoticeApplicationDate") LocalDate hearingNoticeApplicationDate) {
        this.hearingNoticeApplicationDetail = hearingNoticeApplicationDetail;
        this.hearingNoticeApplicationType = hearingNoticeApplicationType;
        this.hearingNoticeApplicationDate = hearingNoticeApplicationDate;
    }
}
