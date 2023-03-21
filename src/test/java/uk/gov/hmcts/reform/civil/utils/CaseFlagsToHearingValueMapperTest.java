package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsToHearingValueMapper.*;

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
    public void testHasLanguageMisinterpretationFlag() {
        FlagDetail flagDetail1 = FlagDetail.builder()
            .status("Active")
            .hearingRelevant(YES)
            .flagCode("PF0015")
            .subTypeKey("english")
            .build();

        FlagDetail flagDetail2 = FlagDetail.builder()
            .status("INACTIVE")
            .hearingRelevant(YES)
            .flagCode("PF0015")
            .build();

        List<FlagDetail> flagDetails = List.of(flagDetail1, flagDetail2);

        assertEquals("english", hasLanguageMisinterpretationFlag(flagDetails));
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
