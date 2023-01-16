package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum BundlingDocGroupEnum {
    @JsonProperty("applicantApplication")
    applicantApplication("applicantApplication", "applicantApplication"),

    @JsonProperty("applicantC1AApplication")
    applicantC1AApplication("applicantC1AApplication", "applicantC1AApplication"),

    @JsonProperty("applicantC1AResponse")
    applicantC1AResponse("applicantC1AResponse", "applicantC1AResponse"),

    @JsonProperty("applicantAppicationsWithinProceedings")
    applicantAppicationsWithinProceedings("applicantAppicationsWithinProceedings",
                                          "applicantAppicationsWithinProceedings"),

    @JsonProperty("applicantMiamCertificate")
    applicantMiamCertificate("applicantMiamCertificate", "applicantMiamCertificate"),

    @JsonProperty("applicantPreviousOrdersSubmittedWithApplication")
    applicantPreviousOrdersSubmittedWithApplication("applicantPreviousOrdersSubmittedWithApplication",
        "applicantPreviousOrdersSubmittedWithApplication"),

    @JsonProperty("respondentApplication")
    respondentApplication("respondentApplication", "respondentApplication"),

    @JsonProperty("respondentC1AApplication")
    respondentC1AApplication("respondentC1AApplication", "respondentC1AApplication"),

    @JsonProperty("respondentC1AResponse")
    respondentC1AResponse("respondentC1AResponse", "respondentC1AResponse"),

    @JsonProperty("respondentAppicationsFromOtherProceedings")
    respondentAppicationsFromOtherProceedings("respondentAppicationsFromOtherProceedings",
                                              "respondentAppicationsFromOtherProceedings"),

    @JsonProperty("ordersSubmittedWithApplication")
    ordersSubmittedWithApplication("ordersSubmittedWithApplication",
                                   "ordersSubmittedWithApplication"),

    @JsonProperty("approvedOrders")
    approvedOrders("approvedOrders", "approvedOrders"),

    @JsonProperty("applicantPositionStatements")
    applicantPositionStatements("applicantPositionStatements", "applicantPositionStatements"),

    @JsonProperty("respondentPositionStatements")
    respondentPositionStatements("respondentPositionStatements", "respondentPositionStatements"),

    @JsonProperty("applicantWitnessStatements")
    applicantWitnessStatements("applicantWitnessStatements", "applicantWitnessStatements"),

    @JsonProperty("respondentWitnessStatements")
    respondentWitnessStatements("respondentWitnessStatements", "respondentWitnessStatements"),

    @JsonProperty("applicantLettersFromSchool")
    applicantLettersFromSchool("applicantLettersFromSchool", "applicantLettersFromSchool"),

    @JsonProperty("respondentLettersFromSchool")
    respondentLettersFromSchool("respondentLettersFromSchool", "respondentLettersFromSchool"),

    @JsonProperty("otherWitnessStatements")
    otherWitnessStatements("otherWitnessStatements", "otherWitnessStatements"),

    @JsonProperty("applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles")
    applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles("applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles",
        "applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles"),

    @JsonProperty("respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles")
    respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles("respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles",
        "respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles"),

    @JsonProperty("expertMedicalReports")
    expertMedicalReports("expertMedicalReports", "expertMedicalReports"),

    @JsonProperty("expertMedicalRecords")
    expertMedicalRecords("expertMedicalRecords", "expertMedicalRecords"),

    @JsonProperty("expertDNAReports")
    expertDNAReports("expertDNAReports", "expertDNAReports"),

    @JsonProperty("expertReportsForDrugAndAlcholTest")
    expertReportsForDrugAndAlcholTest("expertReportsForDrugAndAlcholTest", "expertReportsForDrugAndAlcholTest"),

    @JsonProperty("policeReports")
    policeReports("policeReports", "policeReports"),

    @JsonProperty("expertReportsUploadedByCourtAdmin")
    expertReportsUploadedByCourtAdmin("expertReportsUploadedByCourtAdmin",
                                      "expertReportsUploadedByCourtAdmin"),

    @JsonProperty("cafcassReportsUploadedByCourtAdmin")
    cafcassReportsUploadedByCourtAdmin("cafcassReportsUploadedByCourtAdmin",
                                       "cafcassReportsUploadedByCourtAdmin"),

    @JsonProperty("applicantStatementDocsUploadedByCourtAdmin")
    applicantStatementDocsUploadedByCourtAdmin("applicantStatementDocsUploadedByCourtAdmin",
                                               "applicantStatementDocsUploadedByCourtAdmin"),

    @JsonProperty("c7Documents")
    c7Documents("c7Documents", "c7Documents"),

    @JsonProperty("notRequiredGroup")
    notRequiredGroup("notRequiredGroup", "notRequiredGroup");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static BundlingDocGroupEnum getValue(String key) {
        return BundlingDocGroupEnum.valueOf(key);
    }
}
