package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FreeFormOrderValues {

    private String onInitiativeSelectionTextArea;
    private LocalDate onInitiativeSelectionDate;
    private String withoutNoticeSelectionTextArea;
    private LocalDate withoutNoticeSelectionDate;

}
