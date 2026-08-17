package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class HelpWithFeesMoreInformation {

    @CCD(
            label = "Proof of the following needed (Select one or more)",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "HwfInformationProofList"
    )
    private List<HwFMoreInfoRequiredDocuments> hwFMoreInfoRequiredDocuments;
    @CCD(label = "Details", searchable = false, typeOverride = FieldType.TextArea)
    private String hwFMoreInfoDetails;
    @CCD(label = "Documents to be sent before 4pm", searchable = false)
    private LocalDate hwFMoreInfoDocumentDate;
}
