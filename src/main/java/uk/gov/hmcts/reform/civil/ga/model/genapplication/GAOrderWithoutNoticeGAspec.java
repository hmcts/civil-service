package uk.gov.hmcts.reform.civil.ga.model.genapplication;

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
public class GAOrderWithoutNoticeGAspec {

    private String orderWithoutNotice;
    private LocalDate orderWithoutNoticeDate;

    @JsonCreator
    GAOrderWithoutNoticeGAspec(@JsonProperty("orderWithoutNotice") String orderWithoutNotice,
                                    @JsonProperty("orderWithoutNoticeDate") LocalDate  orderWithoutNoticeDate) {

        this.orderWithoutNotice = orderWithoutNotice;
        this.orderWithoutNoticeDate = orderWithoutNoticeDate;
    }
}
