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
public class AppealTypeChoiceList {

    private LocalDate appealGrantedRefusedDate;

    @JsonCreator
    AppealTypeChoiceList(@JsonProperty("assistedOrderAppealDate") LocalDate appealGrantedRefusedDate) {

        this.appealGrantedRefusedDate = appealGrantedRefusedDate;
    }
}
