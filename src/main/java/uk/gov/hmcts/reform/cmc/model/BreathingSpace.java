package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BreathingSpace {

    private String bsReferenceNumber;
    private LocalDate bsEnteredDate;
    private LocalDate bsLiftedDate;
    private LocalDate bsEnteredDateByInsolvencyTeam;
    private LocalDate bsLiftedDateByInsolvencyTeam;
    private LocalDate bsExpectedEndDate;
    private String bsLiftedFlag;

    @JsonIgnore
    public boolean applies(){
        return  bsEnteredDate != null
            && bsLiftedDate == null
            && (bsExpectedEndDate == null || bsExpectedEndDate.isBefore(LocalDate.now()));
    }

}
