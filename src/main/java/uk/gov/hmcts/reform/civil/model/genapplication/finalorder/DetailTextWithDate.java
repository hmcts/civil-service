package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class DetailTextWithDate {

    private final String detailText;
    private final LocalDate date;

    @JsonCreator
    DetailTextWithDate(@JsonProperty("detailText") String detailText,
                       @JsonProperty("date") LocalDate date) {
        this.detailText = detailText;
        this.date = date;
    }
}
