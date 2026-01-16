package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class HelpWithFeesMoreInformation {

    private List<HwFMoreInfoRequiredDocuments> hwFMoreInfoRequiredDocuments;
    private String hwFMoreInfoDetails;
    private LocalDate hwFMoreInfoDocumentDate;
}
