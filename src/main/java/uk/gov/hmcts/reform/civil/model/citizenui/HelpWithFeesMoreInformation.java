package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpWithFeesMoreInformation {

    private List<HwFMoreInfoRequiredDocuments> hwFMoreInfoRequiredDocuments;
    private String hwFMoreInfoDetails;
    private LocalDate hwFMoreInfoDocumentDate;
}
