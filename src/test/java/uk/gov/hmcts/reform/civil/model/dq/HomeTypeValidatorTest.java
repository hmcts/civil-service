package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionSpec;

public class HomeTypeValidatorTest {

    private static final HomeTypeValidator validator = new HomeTypeValidator();

    @Test
    public void typeInvalid() {
        Assert.assertFalse(validator.isValid(
            new HomeDetails(null, null),
            null
        ));

        Assert.assertFalse(validator.isValid(
            new HomeDetails(HomeTypeOptionSpec.OTHER, null),
            null
        ));

        Assert.assertFalse(validator.isValid(
            new HomeDetails(HomeTypeOptionSpec.OTHER, " "),
            null
        ));
    }

    @Test
    public void validInstances() {
        Assert.assertTrue(validator.isValid(
            new HomeDetails(HomeTypeOptionSpec.JOINTLY_OWNED_HOME, null),
            null
        ));

        Assert.assertTrue(validator.isValid(
            new HomeDetails(HomeTypeOptionSpec.OTHER, "not blank"),
            null
        ));
    }
}
