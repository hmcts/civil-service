package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
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
