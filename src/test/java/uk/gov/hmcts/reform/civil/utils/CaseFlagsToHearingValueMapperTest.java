package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getAdditionalSecurity;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getCustodyStatus;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getInterpreterLanguage;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getOtherReasonableAdjustmentDetails;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getReasonableAdjustments;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getVulnerabilityDetails;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.hasCaseInterpreterRequiredFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.hasVulnerableFlag;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CaseFlagsToHearingValueMapperTest {

    @Test
    public void testHasVulnerableFlag() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setFlagCode("PF0002");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setFlagCode("RA0033");

        FlagDetail flagDetail3 = new FlagDetail()
            .setStatus("INACTIVE")
            .setFlagCode("PF0002");

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

        assertEquals(true, hasVulnerableFlag(flagDetails));
    }

    @Test
    public void testHasAdditionalSecurityFlag() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setFlagCode("PF0007");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setFlagCode("PF0007");

        FlagDetail flagDetail3 = new FlagDetail()
            .setStatus("INACTIVE")
            .setFlagCode("PF0007");

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

        assertEquals(true, getAdditionalSecurity(flagDetails));
    }

    @Nested
    class GetLanguageInterpreter {
        @Test
        public void shouldReturnLanguageInterpreterFlag() {
            FlagDetail flagDetail1 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015")
                .setSubTypeKey("fra")
                .setSubTypeValue("French");

            FlagDetail flagDetail2 = new FlagDetail()
                .setStatus("INACTIVE")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015");

            List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2);

            assertEquals("fra", getInterpreterLanguage(flagDetails));
        }

        @Test
        public void shouldReturnNullWhenNoSubValueKey() {
            FlagDetail flagDetail1 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015")
                .setSubTypeValue("random");

            FlagDetail flagDetail2 = new FlagDetail()
                .setStatus("INACTIVE")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015");

            FlagDetail flagDetail3 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setSubTypeValue("American Sign Language")
                .setFlagCode("RA0042");

            List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

            assertEquals(null, getInterpreterLanguage(flagDetails));
        }

        @Test
        public void shouldReturnNullWhenNoFlags() {
            List<FlagDetail> flagDetails = List.of();

            assertEquals(null, getInterpreterLanguage(flagDetails));
        }

        @Test
        public void shouldReturnSignLanguageKeyWhenNoSpokenLanguageKey() {
            FlagDetail flagDetail1 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015")
                .setSubTypeValue("random");

            FlagDetail flagDetail2 = new FlagDetail()
                .setStatus("INACTIVE")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015");

            FlagDetail flagDetail3 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setSubTypeKey("sign-sse")
                .setSubTypeValue("Speech Supported English (SSE)")
                .setFlagCode("RA0042");

            List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

            assertEquals("sign-sse", getInterpreterLanguage(flagDetails));
        }

        @Test
        public void shouldReturnFirstSpokenLanguageInterpreterFlag() {
            FlagDetail flagDetail1 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015")
                .setSubTypeKey("fra")
                .setSubTypeValue("French");

            FlagDetail flagDetail2 = new FlagDetail()
                .setStatus("INACTIVE")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015");

            FlagDetail flagDetail3 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015")
                .setSubTypeKey("wel")
                .setSubTypeValue("WELSH");

            FlagDetail flagDetail4 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setSubTypeKey("sign-sse")
                .setSubTypeValue("Speech Supported English (SSE)")
                .setFlagCode("RA0042");

            List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3, flagDetail4);

            assertEquals("fra", getInterpreterLanguage(flagDetails));
        }

        @Test
        public void shouldReturnFirstSignLanguageInterpreterFlag() {
            FlagDetail flagDetail1 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setSubTypeKey("sign-sse")
                .setSubTypeValue("Speech Supported English (SSE)")
                .setFlagCode("RA0042");

            FlagDetail flagDetail2 = new FlagDetail()
                .setStatus("INACTIVE")
                .setHearingRelevant(YES)
                .setFlagCode("PF0015");

            FlagDetail flagDetail3 = new FlagDetail()
                .setStatus("Active")
                .setHearingRelevant(YES)
                .setSubTypeKey("sign")
                .setSubTypeValue("Some other sign")
                .setFlagCode("RA0042");

            List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

            assertEquals("sign-sse", getInterpreterLanguage(flagDetails));
        }

    }

    @Test
    public void shouldReturnCorrectFlagsForOtherReasonableAdjustmentDetails() {
        FlagDetail flagNotRAorSM = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("PF0002");

        FlagDetail flagWithDetailsAndComments = new FlagDetail()
            .setStatus("Active")
            .setName("Flight risk")
            .setFlagComment("comment")
            .setFlagCode("SM001")
            .setHearingRelevant(YES)
            .setStatus("Active");

        FlagDetail flagDetailsOnly = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0019")
            .setName("Step free / wheelchair access")
            .setSubTypeKey("whe")
            .setSubTypeValue("WHEELCHAIR_ACCESS_FLAG");

        FlagDetail flagCommentsOnly = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0026")
            .setFlagComment("Support comment")
            .setSubTypeKey("sup")
            .setSubTypeValue("SUPPORT_WORKER_FLAG");

        FlagDetail flagLanguageManualEntry = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("PF0015")
            .setSubTypeValue("Hokkien")
            .setName("Language Interpreter")
            .setFlagComment("Local dialect");

        FlagDetail flagSignLanguageManualEntry = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0042")
            .setName("Sign Language Interpreter")
            .setSubTypeValue("Hokkien")
            .setFlagComment("Local dialect");

        FlagDetail flagSecondLanguage = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("PF0015")
            .setSubTypeValue("French")
            .setName("Language Interpreter")
            .setFlagComment("Comment - Waterloo accent");

        List<FlagDetail> flagDetails = List.of(
            flagNotRAorSM,
            flagWithDetailsAndComments,
            flagDetailsOnly,
            flagCommentsOnly,
            flagLanguageManualEntry,
            flagSignLanguageManualEntry,
            flagSecondLanguage
        );

        assertEquals(
            "SM001: Flight risk: comment, RA0019: Step free / wheelchair access, "
                + "RA0026: Support comment, RA0042: Sign Language Interpreter: Local dialect: Hokkien, "
                + "PF0015: Language Interpreter: Local dialect: Hokkien, "
                + "PF0015: Language Interpreter: Comment - Waterloo accent: French",
            getOtherReasonableAdjustmentDetails(flagDetails)
        );
    }

    @Test
    public void testHasCaseInterpreterRequiredFlag() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("PF0015")
            .setSubTypeValue("english");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("INACTIVE")
            .setHearingRelevant(YES)
            .setFlagCode("PF0015");

        FlagDetail flagDetail3 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0042");

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().flags(new Flags()
                                                  .setDetails(wrapElements(flagDetail1)))
                            .build())
            .respondent1(Party.builder().flags(new Flags()
                                                   .setDetails(wrapElements(flagDetail2, flagDetail3)))
                             .build())
                .build();

        assertTrue(hasCaseInterpreterRequiredFlag(caseData));
    }

    @Test
    public void testGetCustodyStatus() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setFlagCode("PF0019");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setFlagCode("PF0019");

        FlagDetail flagDetail3 = new FlagDetail()
            .setStatus("INACTIVE")
            .setFlagCode("PF0019");

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

        assertEquals("C", getCustodyStatus(flagDetails));
    }

    @Test
    public void testHasReasonableAdjustments() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0033")
            .setName("Private waiting area")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("SM0002")
            .setName("Screening witness from accused")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail3 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0026")
            .setName("Support worker or carer with me");

        FlagDetail flagDetail4 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0042")
            .setName("Sign Language Interpreter")
            .setFlagComment("a sign language comment");

        List<String> expected = List.of(
            "RA0033",
            "SM0002",
            "RA0026",
            "RA0042"
        );

        List<String> actualReasonableAdjustments = getReasonableAdjustments(
            List.of(
                flagDetail1,
                flagDetail2,
                flagDetail3,
                flagDetail4
            ));

        assertEquals(expected, actualReasonableAdjustments);
    }

    @Test
    public void testDoesNotHaveReasonableAdjustments() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(NO)
            .setFlagCode("SM0002")
            .setName("Screening witness from accused")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("SN0002")
            .setName("Screening witness from accused")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail3 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("00RA26")
            .setName("Support worker or carer with me");

        FlagDetail flagDetail4 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("OT0001")
            .setName("Other");

        FlagDetail flagDetail5 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("R00A42")
            .setName("Sign Language Interpreter")
            .setFlagComment("a sign language comment");

        FlagDetail flagDetail6 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(NO)
            .setFlagCode("RA0042")
            .setName("Sign Language Interpreter")
            .setFlagComment("a sign language comment");

        List<String> expected = List.of(
            "RA0033",
            "SM0002",
            "RA0026",
            "RA0042"
        );

        List<String> actualReasonableAdjustments = getReasonableAdjustments(
            List.of(
                flagDetail1,
                flagDetail2,
                flagDetail3,
                flagDetail4,
                flagDetail5
            ));

        assertTrue(actualReasonableAdjustments.isEmpty());
    }

    @Test
    public void testWithNoHearingRelevant() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(NO)
            .setFlagCode("RE0033")
            .setName("Private waiting area")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(NO)
            .setFlagCode("RA0042")
            .setName("Sign Language Interpreter")
            .setFlagComment("a sign language comment");

        List<String> expected = List.of(
            "RA0033",
            "SM0002",
            "RA0026",
            "RA0042"
        );

        List<String> actualReasonableAdjustments = getReasonableAdjustments(
            List.of(
                flagDetail1,
                flagDetail2
            ));

        assertTrue(actualReasonableAdjustments.isEmpty());
    }

    @Test
    public void testGetVulnerabilityDetails() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0033")
            .setName("Private waiting area")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("SM0002")
            .setName("Screening witness from accused")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail3 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("RA0026")
            .setName("Support worker or carer with me");

        FlagDetail flagDetail4 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(YES)
            .setFlagCode("PF0002")
            .setName("Vulnerable user")
            .setFlagComment("this is a comment");

        String expected =
            "Private waiting area - this is a comment; " +
                "Support worker or carer with me; " +
                "Vulnerable user - this is a comment";

        String actualVulnerabilityDetails = getVulnerabilityDetails(
            List.of(
                flagDetail1,
                flagDetail2,
                flagDetail3,
                flagDetail4
            ));

        assertEquals(expected, actualVulnerabilityDetails);
    }

    @Test
    public void testNoVulnerabilityDetails() {
        FlagDetail flagDetail1 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(NO)
            .setFlagCode("RE0033")
            .setName("Private waiting area")
            .setFlagComment("this is a comment");

        FlagDetail flagDetail2 = new FlagDetail()
            .setStatus("Active")
            .setHearingRelevant(NO)
            .setFlagCode("RA0042")
            .setName("Sign Language Interpreter")
            .setFlagComment("a sign language comment");

        String actualVulnerabilityDetails = getVulnerabilityDetails(
            List.of(
                flagDetail1,
                flagDetail2
            ));

        assertThat(actualVulnerabilityDetails).isEqualTo(null);
    }
}
