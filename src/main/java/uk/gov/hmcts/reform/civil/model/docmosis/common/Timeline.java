package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Timeline {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate timelineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timelineDescription;

    @JsonCreator
    public Timeline(@JsonProperty("timelineDate") LocalDate timelineDate,
                    @JsonProperty("timelineDescription") String timelineDescription) {
        this.timelineDate = timelineDate;
        this.timelineDescription = timelineDescription;
    }

}

