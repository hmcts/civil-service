package uk.gov.hmcts.reform.civil.service.directionsorder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ALTERNATIVE_DISPUTE_RESOLUTION_LABEL;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.ALTERNATIVE_DISPUTE_RESOLUTION_PARAGRAPH;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLAIM_SETTLING_CONSENT_ORDER_LABEL;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CLAIM_SETTLING_CONSENT_ORDER_PARAGRAPH;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.CREDIT_HIRE_NON_COMPLIANCE_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DIGITAL_PORTAL_BUNDLE_WARNING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SETTLEMENT_DIGITAL_PORTAL_LABEL;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SETTLEMENT_DIGITAL_PORTAL_PARAGRAPH;

/**
 * Guards against CCD label drift for the text that also appears in runtime services.
 */
class DirectionsOrderTextLibraryCcdParityTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void fastTrackDigitalPortalBundleWarningMatchesCcdLabel() throws IOException {
        assertCaseFieldLabelEquals(
            Path.of("ccd-definition/CaseField/CaseField-SDO.json"),
            "fastTrackTrialBundleText",
            FAST_TRACK_DIGITAL_PORTAL_BUNDLE_WARNING
        );
    }

    @Test
    void creditHireNonComplianceEventLabelsMirrorLibrary() throws IOException {
        assertEventLabelMatches(
            Path.of("ccd-definition/CaseEventToComplexTypes/CreateSDO-SDO.json"),
            "smallClaimsCreditHire",
            "text",
            CREDIT_HIRE_NON_COMPLIANCE_SDO
        );

        assertEventLabelMatches(
            Path.of("ccd-definition/CaseEventToComplexTypes/CreateSDO-SDO.json"),
            "fastTrackCreditHire",
            "text",
            CREDIT_HIRE_NON_COMPLIANCE_SDO
        );

        assertEventLabelMatches(
            Path.of("ccd-definition/CaseEventToComplexTypes/CreateSDO-SDO-R2.json"),
            "sdoR2FastTrackCreditHire",
            "text",
            CREDIT_HIRE_NON_COMPLIANCE_SDO
        );
    }

    @Test
    void alternativeDisputeResolutionTextMatchesCcdDefinitions() throws IOException {
        assertCaseFieldLabelEquals(
            Path.of("ccd-definition/CaseField/CaseFieldSDO-DJ.json"),
            "trialFastTrackAltDisputeResolution",
            ALTERNATIVE_DISPUTE_RESOLUTION_LABEL
        );

        assertComplexTypeElementLabelMatches(
            Path.of("ccd-definition/ComplexTypes/FastTrackAltDisputeResolution-SDO.json"),
            "text",
            ALTERNATIVE_DISPUTE_RESOLUTION_PARAGRAPH
        );

        assertComplexTypeElementLabelMatches(
            Path.of("ccd-definition/ComplexTypes/SDOR2/FastTrackAltDisputeResolution-SDO-R2.json"),
            "label",
            ALTERNATIVE_DISPUTE_RESOLUTION_PARAGRAPH
        );
    }

    @Test
    void settlementTextMatchesCcdDefinitions() throws IOException {
        assertCaseFieldLabelEquals(
            Path.of("ccd-definition/CaseField/CaseFieldSDO-DJ.json"),
            "trialFastTrackSettlement",
            SETTLEMENT_DIGITAL_PORTAL_LABEL
        );

        assertComplexTypeElementLabelMatches(
            Path.of("ccd-definition/ComplexTypes/FastTrackSettlement-SDO.json"),
            "text",
            SETTLEMENT_DIGITAL_PORTAL_PARAGRAPH
        );

        assertComplexTypeElementLabelMatches(
            Path.of("ccd-definition/ComplexTypes/SDOR2/Settlement-SDO-R2.json"),
            "label",
            SETTLEMENT_DIGITAL_PORTAL_PARAGRAPH
        );
    }

    @Test
    void claimSettlingTextMatchesCcdDefinitions() throws IOException {
        assertCaseFieldLabelEquals(
            Path.of("ccd-definition/CaseField/CaseFieldSDO-DJ.json"),
            "disposalHearingClaimSettlingDJ",
            CLAIM_SETTLING_CONSENT_ORDER_LABEL
        );

        assertComplexTypeElementLabelMatches(
            Path.of("ccd-definition/ComplexTypes/DisposalHearingClaimSettling-SDO.json"),
            "text",
            CLAIM_SETTLING_CONSENT_ORDER_PARAGRAPH
        );
    }

    @Test
    void telephoneAndEmailStatementsAlignAcrossSdoAndDj() throws IOException {
        String sdoEmail = getCaseFieldLabel(
            Path.of("ccd-definition/CaseField/CaseField-SDO.json"),
            "disposalHearingMethodVideoConferenceHearingCourtStatement"
        );
        String djEmail = getCaseFieldLabel(
            Path.of("ccd-definition/CaseField/CaseFieldSDO-DJ.json"),
            "disposalHearingMethodVideoConferenceHearingCourtStatementDJ"
        );

        assertThat(sdoEmail)
            .as("Video hearing contact instructions should be identical for SDO and DJ")
            .isEqualTo(djEmail);

        String sdoTelephone = getCaseFieldLabel(
            Path.of("ccd-definition/CaseField/CaseField-SDO.json"),
            "disposalHearingMethodTelephoneHearingCourtStatement"
        );
        String djTelephone = getCaseFieldLabel(
            Path.of("ccd-definition/CaseField/CaseFieldSDO-DJ.json"),
            "disposalHearingMethodTelephoneHearingCourtStatementDJ"
        );

        assertThat(sdoTelephone)
            .as("Telephone hearing contact instructions should be identical for SDO and DJ")
            .isEqualTo(djTelephone);
    }

    @Test
    void variationOfDirectionsParagraphMatchesAcrossArtifacts() throws IOException {
        String djVariation = getCaseFieldLabel(
            Path.of("ccd-definition/CaseField/CaseFieldSDO-DJ.json"),
            "trialFastTrackVariationOfDirections"
        );
        String fastTrackVariation = getComplexTypeElementLabel(
            Path.of("ccd-definition/ComplexTypes/FastTrackVariationOfDirections-SDO.json"),
            "text"
        );
        String r2Variation = getComplexTypeElementLabel(
            Path.of("ccd-definition/ComplexTypes/SDOR2/VariationOfDirections-SDO-R2.json"),
            "label"
        );

        assertThat(normalizeParagraph(djVariation))
            .as("DJ variation text should contain the shared paragraph")
            .contains(normalizeParagraph(fastTrackVariation));

        assertThat(normalizeParagraph(r2Variation))
            .as("SDO R2 variation paragraph should mirror the fast-track definition")
            .isEqualTo(normalizeParagraph(fastTrackVariation));
    }

    @Test
    void smallClaimsMediationWarningMatchesAcrossCcdFiles() throws IOException {
        String drhWarning = getCaseFieldLabel(
            Path.of("ccd-definition/CaseField/CaseField-SDO-R2.json"),
            "sdoR2SmallClaimsWarning2"
        );
        String orderDetailsWarning = getComplexTypeElementLabel(
            Path.of("ccd-definition/ComplexTypes/SmallClaimsOrderAndHearingDetails-SDO.json"),
            "text2"
        );

        assertThat(drhWarning)
            .as("Small-claims mediation warning should reference the mediation service")
            .contains("Small Claims Mediation Service");
        assertThat(orderDetailsWarning)
            .as("Order details paragraph should reference the mediation service")
            .contains("Small Claims Mediation Service");
        assertThat(drhWarning.toLowerCase())
            .as("Small-claims warning should mention the service being free")
            .contains("service is free");
        assertThat(orderDetailsWarning.toLowerCase())
            .as("Order details warning should mention the service being free")
            .contains("service is free");
        assertThat(drhWarning.replaceAll("\\D", ""))
            .as("Phone number should match across CCD artefacts")
            .isEqualTo(orderDetailsWarning.replaceAll("\\D", ""));
    }

    private void assertEventLabelMatches(Path path,
                                         String caseFieldId,
                                         String listElementCode,
                                         String expectedValue) throws IOException {
        String actual = getEventElementLabel(path, caseFieldId, listElementCode);
        assertThat(actual)
            .as("%s.%s EventElementLabel should mirror %s", caseFieldId, listElementCode, expectedValue)
            .isEqualTo(expectedValue);
    }

    private void assertCaseFieldLabelEquals(Path path, String fieldId, String expectedValue) throws IOException {
        String actual = getCaseFieldLabel(path, fieldId);
        assertThat(actual)
            .as("%s label should mirror %s", fieldId, expectedValue)
            .isEqualTo(expectedValue);
    }

    private void assertComplexTypeElementLabelMatches(Path path,
                                                      String listElementCode,
                                                      String expectedValue) throws IOException {
        String actual = getComplexTypeElementLabel(path, listElementCode);
        assertThat(actual)
            .as("%s ElementLabel in %s should mirror %s", listElementCode, path, expectedValue)
            .isEqualTo(expectedValue);
    }

    private String getEventElementLabel(Path path,
                                        String caseFieldId,
                                        String listElementCode) throws IOException {
        Map<String, Object> fieldEntry = readEntries(path).stream()
            .filter(entry -> caseFieldId.equals(entry.get("CaseFieldID")))
            .filter(entry -> listElementCode.equals(entry.get("ListElementCode")))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                "Missing " + caseFieldId + "." + listElementCode + " entry in " + path
            ));

        return (String) fieldEntry.get("EventElementLabel");
    }

    private String getCaseFieldLabel(Path path, String fieldId) throws IOException {
        Map<String, Object> fieldEntry = readEntries(path).stream()
            .filter(entry -> fieldId.equals(entry.get("ID")))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                fieldId + " missing from " + path
            ));

        return (String) fieldEntry.get("Label");
    }

    private String getComplexTypeElementLabel(Path path,
                                              String listElementCode) throws IOException {
        Map<String, Object> entry = readEntries(path).stream()
            .filter(map -> listElementCode.equals(map.get("ListElementCode")))
            .findFirst()
            .orElseThrow(() -> new AssertionError(
                listElementCode + " missing from " + path
            ));

        return (String) entry.get("ElementLabel");
    }

    private String normalizeParagraph(String value) {
        return value.replaceAll("[\\s(),.]+", "").toLowerCase();
    }

    private List<Map<String, Object>> readEntries(Path path) throws IOException {
        return OBJECT_MAPPER.readValue(
            Files.newBufferedReader(path),
            new TypeReference<>() {
            }
        );
    }
}
