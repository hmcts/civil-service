package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getAdditionalSecurity;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getCustodyStatus;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getInterpreterLanguage;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.getReasonableAdjustments;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.hasCaseInterpreterRequiredFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.hasVulnerableFlag;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CaseFlagsToHearingValueMapperTest {

    @Test
    public void testHasVulnerableFlag() {
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .flagCode("PF0002")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("Active")
            .flagCode("RA0033")
            .build();

        FlagDetail flagDetail3 = FlagDetail.builder()
            .status("INACTIVE")
            .flagCode("PF0002")
            .build();

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

        assertEquals(true, hasVulnerableFlag(flagDetails));
    }

    @Test
    public void testHasAdditionalSecurityFlag() {
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .flagCode("PF0007")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("Active")
            .flagCode("PF0007")
            .build();

        FlagDetail flagDetail3 = FlagDetail.builder()
            .status("INACTIVE")
            .flagCode("PF0007")
            .build();

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

        assertEquals(true, getAdditionalSecurity(flagDetails));
    }

    @Test
    public void testHasLanguageInterpreterFlag() {
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("PF0015")
            .subTypeValue("english")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("INACTIVE")
            .hearingRelevant(YES)
            .flagCode("PF0015")
            .build();

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2);

        assertEquals("english", getInterpreterLanguage(flagDetails));
    }

    @Test
    public void testHasCaseInterpreterRequiredFlag() {
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("PF0015")
            .subTypeValue("english")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("INACTIVE")
            .hearingRelevant(YES)
            .flagCode("PF0015")
            .build();

        FlagDetail flagDetail3 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("RA0042")
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().flags(Flags.builder()
                                                  .details(wrapElements(flagDetail1))
                                                  .build())
                            .build())
            .respondent1(Party.builder().flags(Flags.builder()
                                                   .details(wrapElements(flagDetail2, flagDetail3))
                                                   .build())
                             .build())
            .build();

        assertTrue(hasCaseInterpreterRequiredFlag(caseData));
    }

    @Test
    public void testGetCustodyStatus() {
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .flagCode("PF0019")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("Active")
            .flagCode("PF0019")
            .build();

        FlagDetail flagDetail3 = FlagDetail.builder()
            .status("INACTIVE")
            .flagCode("PF0019")
            .build();

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2, flagDetail3);

        assertEquals("C", getCustodyStatus(flagDetails));
    }

    @Test
    public void testHasReasonableAdjustments() {
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("RA0033")
            .name("Private waiting area")
            .flagComment("this is a comment")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("SM0002")
            .name("Screening witness from accused")
            .flagComment("this is a comment")
            .build();

        FlagDetail flagDetail3 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("RA0026")
            .name("Support worker or carer with me")
            .build();

        FlagDetail flagDetail4 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("RA0042")
            .name("Sign Language Interpreter")
            .flagComment("a sign language comment")
            .build();

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
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(NO)
            .flagCode("SM0002")
            .name("Screening witness from accused")
            .flagComment("this is a comment")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("SN0002")
            .name("Screening witness from accused")
            .flagComment("this is a comment")
            .build();

        FlagDetail flagDetail3 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("00RA26")
            .name("Support worker or carer with me")
            .build();

        FlagDetail flagDetail4 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("OT0001")
            .name("Other")
            .build();

        FlagDetail flagDetail5 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("R00A42")
            .name("Sign Language Interpreter")
            .flagComment("a sign language comment")
            .build();

        FlagDetail flagDetail6 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(NO)
            .flagCode("RA0042")
            .name("Sign Language Interpreter")
            .flagComment("a sign language comment")
            .build();

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
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(NO)
            .flagCode("RE0033")
            .name("Private waiting area")
            .flagComment("this is a comment")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(NO)
            .flagCode("RA0042")
            .name("Sign Language Interpreter")
            .flagComment("a sign language comment")
            .build();

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
}
