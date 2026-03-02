package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.ga.enums.MakeAppAvailableCheckGAspec;

import java.util.List;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GAMakeApplicationAvailableCheck {

    private List<MakeAppAvailableCheckGAspec> makeAppAvailableCheck;

    @JsonCreator
    GAMakeApplicationAvailableCheck(@JsonProperty("makeAppAvailableCheck") List<MakeAppAvailableCheckGAspec>
                                        makeAppAvailableCheck) {
        this.makeAppAvailableCheck = makeAppAvailableCheck;
    }
}
