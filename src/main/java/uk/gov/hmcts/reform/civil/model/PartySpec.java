package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.access.ApplicantAccess;
import uk.gov.hmcts.reform.civil.access.CaseworkerCaaAccess;
import uk.gov.hmcts.reform.civil.enums.PartyTypeSpec;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class PartySpec {

    @CCD(label = "Claimant type",
            typeOverride = FixedRadioList,
            typeParameterOverride = "PartyType",
            access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    private PartyTypeSpec typeSpec;

    @CCD(typeOverride = TextArea,
            access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String individualTitleSpec;

    @CCD(typeOverride = TextArea,
            access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String individualFirstNameSpec;

    @CCD(typeOverride = TextArea,
            access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String individualLastNameSpec;

    @CCD(access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate individualDateOfBirthSpec;

    @CCD(typeOverride = TextArea,
            access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String companyNameSpec;

    @CCD(typeOverride = TextArea,
            access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String organisationNameSpec;

}
