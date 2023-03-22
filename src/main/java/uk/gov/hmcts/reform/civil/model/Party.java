package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.access.ApplicantAccess;
import uk.gov.hmcts.reform.civil.access.CaseworkerCaaAccess;
import uk.gov.hmcts.reform.civil.enums.PartyType;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class Party {

    @CCD(label = "Claimant type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "PartyType",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    private PartyType type;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String individualTitle;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String individualFirstName;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String individualLastName;

    @CCD(access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate individualDateOfBirth;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String companyName;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String organisationName;

    @CCD(typeOverride = TextArea,
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class})
    private String soleTraderName;
}
