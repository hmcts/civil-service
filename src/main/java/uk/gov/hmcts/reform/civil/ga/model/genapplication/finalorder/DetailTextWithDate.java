package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;

import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DetailTextWithDate {

    private String detailText;
    private LocalDate date;

    @JsonCreator
    DetailTextWithDate(@JsonProperty("detailText") String detailText,
                       @JsonProperty("date") LocalDate date) {
        this.detailText = detailText;
        this.date = date;
    }
}
