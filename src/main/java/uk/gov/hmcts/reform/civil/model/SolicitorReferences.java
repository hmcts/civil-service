package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.access.ApplicantAccess;
import uk.gov.hmcts.reform.civil.access.CaseworkerCaaAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class SolicitorReferences {

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String applicantSolicitor1Reference;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String respondentSolicitor1Reference;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String respondentSolicitor2Reference;
}
