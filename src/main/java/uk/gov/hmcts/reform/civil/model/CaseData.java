package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.access.ApplicantAccess;

@SuperBuilder(toBuilder = true)
@Jacksonized
@Data
public class CaseData {

    /*@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @CCD(
        access = {ApplicantAccess.class}
    )
    private final Long ccdCaseReference; */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)

    @CCD(
        access = {ApplicantAccess.class}
    )
    private final SolicitorReferences solicitorReferences;

    @JsonUnwrapped
    @CCD(
        access = {ApplicantAccess.class}
    )
    private PartySpec applicant1Spec;
}
