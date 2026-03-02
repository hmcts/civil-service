package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.experimental.Accessors;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AssistedOrderDateHeard {

    private LocalDate singleDate;
    private LocalDate dateRangeFrom;
    private LocalDate dateRangeTo;
    private LocalDate datesToAvoidDates;
    private String beSpokeRangeText;

    @JsonCreator
    AssistedOrderDateHeard(@JsonProperty("singleDateHeard") LocalDate singleDate,
                           @JsonProperty("dateRangeFrom") LocalDate dateRangeFrom,
                           @JsonProperty("dateRangeTo") LocalDate dateRangeTo,
                           @JsonProperty("datesToAvoidDates") LocalDate datesToAvoidDates,
                           @JsonProperty("bespokeRangeTextArea") String beSpokeRangeText) {

        this.singleDate = singleDate;
        this.dateRangeFrom = dateRangeFrom;
        this.dateRangeTo = dateRangeTo;
        this.datesToAvoidDates = datesToAvoidDates;
        this.beSpokeRangeText = beSpokeRangeText;
    }
}
