package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {
        I2PConfirmationTextLRSpecGeneratorTest.AllConfirmationTextGenerator.class
    })
public class I2PConfirmationTextLRSpecGeneratorTest {

    @Autowired
    private List<I2PConfirmationTextLRSpecGenerator> generators;

    /**
     * Checks that (1) each case only activates one generator and (2) each generator is used at least once
     */
    @Test
    public void testNotOverlapping() {
        List<I2PConfirmationTextLRSpecGenerator> usedGenerators = new ArrayList<>();
        List<CaseData> cases = getCases();
        for (int i = 0; i < cases.size(); i++) {
            CaseData currentCase = cases.get(i);
            List<I2PConfirmationTextLRSpecGenerator> suitable = generators.stream()
                .filter(generator -> generator.generateTextFor(currentCase).isPresent())
                .collect(Collectors.toList());
            Assertions.assertEquals(1, suitable.size(),
                                    "There should be exactly 1 suitable generator per case."
            + "Case in position " + i + " has " + suitable.size());
            usedGenerators.addAll(suitable);
        }
        List<I2PConfirmationTextLRSpecGenerator> toBeUsed = new ArrayList<>(generators);
        toBeUsed.removeIf(usedGenerators::contains);
        Assertions.assertTrue(toBeUsed.isEmpty(),
                              "Each generator should be used at least once. Please complete the case list.");
    }

    /**
     * Collections of cases such that all generators are used and only one generator returns not empty for each case.
     *
     * @return list of case data for testNotOverlapping
     */
    private List<CaseData> getCases() {
        return Collections.emptyList();
    }

    /**
     * configuration class to be sure to initialize all implementations of
     * RespondConfirmationTextGenerator
     */
    @Configuration
    @ComponentScan(basePackageClasses = I2PConfirmationTextLRSpecGenerator.class,
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = I2PConfirmationTextLRSpecGenerator.class
        )
    )
    public static class AllConfirmationTextGenerator {

    }
}
