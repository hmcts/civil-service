package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileDirectionsQuestionnaire {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FileDirectionsQuestionnaireConfirm"
    )
    private List<String> explainedToClient;
    @CCD(
            label = "Do you want a one-month stay to try to settle the claim?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo oneMonthStayRequested;
    @CCD(
            label = "Have you complied with the pre-action protocol?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo reactionProtocolCompliedWith;
    @CCD(
            label = "Explain why not",
            showCondition = "reactionProtocolCompliedWith = \"No\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reactionProtocolNotCompliedWithReason;
}
