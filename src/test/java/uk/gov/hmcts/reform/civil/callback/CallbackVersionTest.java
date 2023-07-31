package uk.gov.hmcts.reform.civil.callback;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class CallbackVersionTest {

    @Test
    public void equalOrGreater() {
        for (CallbackVersion v : CallbackVersion.values()) {
            Assertions.assertFalse(v.isEqualOrGreater(null));
        }
        List<CallbackVersion> versions = Arrays.asList(CallbackVersion.values());
        for (int i = 0; i < versions.size(); i++) {
            for (int j = 0; j < versions.size(); j++) {
                Assertions.assertEquals(
                    i >= j,
                    versions.get(i).isEqualOrGreater(versions.get(j))
                );
            }
        }
    }
}
