package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class AppealTypeChoiceList {

    private LocalDate appealGrantedRefusedDate;

    @JsonCreator
    AppealTypeChoiceList(@JsonProperty("assistedOrderAppealDate") LocalDate appealGrantedRefusedDate) {

        this.appealGrantedRefusedDate = appealGrantedRefusedDate;
    }
}
