package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FreeFormOrderValues {

    private String onInitiativeSelectionTextArea;
    private LocalDate onInitiativeSelectionDate;
    private String withoutNoticeSelectionTextArea;
    private LocalDate withoutNoticeSelectionDate;

}
