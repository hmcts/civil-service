package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.getAdditionalSecurity;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.getCustodyStatus;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.getInterpreterLanguage;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.hasCaseInterpreterRequiredFlag;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.hasVulnerableFlag;
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
}
