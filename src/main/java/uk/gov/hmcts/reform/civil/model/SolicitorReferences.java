package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolicitorReferences {

    @CCD(label = "Claimant's legal representative's reference")
    private String applicantSolicitor1Reference;
    @CCD(label = "Defendant's legal representative's reference")
    private String respondentSolicitor1Reference;
    @CCD(
            label = "Defendant's legal representative's reference",
            showCondition = "respondentSolicitor1Reference=\"DO_NOT_SHOW_IN_UI\""
    )
    private String respondentSolicitor2Reference;
}
