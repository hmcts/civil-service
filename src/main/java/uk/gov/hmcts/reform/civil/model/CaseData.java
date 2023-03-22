package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.access.ApplicantAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Label;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CaseData {

    //Civil Service Case Data
    @CCD(
        access = {ApplicantAccess.class}
    )
    private String legacyCaseReference;

    @CCD(
        typeOverride = Label,
        access = {ApplicantAccess.class},
        label = "## Legal representatives: Test Label"
    )
    private String specCheckList;

    @CCD(
        typeOverride = TextArea,
        access = {ApplicantAccess.class},
        label = "Test checklist text"
    )
    private String checkListText;

    @JsonUnwrapped
    @CCD(
        access = {ApplicantAccess.class}
    )
    private SolicitorReferences solicitorReferences;

    @JsonUnwrapped
    @CCD(
        access = {ApplicantAccess.class}
    )
    private Party applicant1Spec;
}
