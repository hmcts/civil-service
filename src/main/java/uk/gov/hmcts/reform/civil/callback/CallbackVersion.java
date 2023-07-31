package uk.gov.hmcts.reform.civil.callback;

import java.util.Arrays;
import java.util.List;

public enum CallbackVersion {
    V_1,
    V_2;

    /**
     * Checks whether a version number is at least this version's number.
     *
     * @param anotherVersion a version number
     * @return true if anotherVersion is not null and anotherVersion is this or above
     */
    public boolean isEqualOrGreater(CallbackVersion anotherVersion) {
        if (anotherVersion == null) {
            return false;
        }
        List<CallbackVersion> list = Arrays.asList(CallbackVersion.values());
        int indexSelf = list.indexOf(this);
        int indexAnother = list.indexOf(anotherVersion);
        return indexSelf >= indexAnother;
    }
}
