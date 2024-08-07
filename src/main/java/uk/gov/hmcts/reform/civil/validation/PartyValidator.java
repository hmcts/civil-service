package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.Address;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyValidator {

    public static final int ADDRESS_MAX_LENGTH_ALLOWED = 35;

    public static final int NAME_MAX_LENGTH_ALLOWED = 70;

    public static final int POST_CODE_MAX_LENGTH = 8;

    public static final String ADDRESS_LENGTH_ERROR = "exceeds maximum length 35";

    public static final String POST_CODE_LENGTH_ERROR = "exceeds maximum length 8";

    public static final String NAME_LENGTH_ERROR = "Name exceeds maximum length 70";

    public static final String[] WELSH_CHARS = {"ˆ", "`", "´", "¨"};

    public static final String WELSH_CHAR_ERROR = "Special characters are not allowed";

    public List<String> validateAddress(Address primaryAddress, List<String> errors) {

        if (exceedsLength(primaryAddress.getAddressLine1(), ADDRESS_MAX_LENGTH_ALLOWED)) {
            errors.add("Building and Street " + ADDRESS_LENGTH_ERROR);
        }
        if (exceedsLength(primaryAddress.getAddressLine2(), ADDRESS_MAX_LENGTH_ALLOWED)) {
            errors.add("Address Line 2 " + ADDRESS_LENGTH_ERROR);
        }
        if (exceedsLength(primaryAddress.getAddressLine3(), ADDRESS_MAX_LENGTH_ALLOWED)) {
            errors.add("Address Line 3 " + ADDRESS_LENGTH_ERROR);
        }
        if (exceedsLength(primaryAddress.getPostTown(), ADDRESS_MAX_LENGTH_ALLOWED)) {
            errors.add("Post town  " + ADDRESS_LENGTH_ERROR);
        }
        if (exceedsLength(primaryAddress.getCounty(), ADDRESS_MAX_LENGTH_ALLOWED)) {
            errors.add("County " + ADDRESS_LENGTH_ERROR);
        }
        if (exceedsLength(primaryAddress.getPostCode(), POST_CODE_MAX_LENGTH)) {
            errors.add("Postcode " + POST_CODE_LENGTH_ERROR);
        }

        if (hasWelshChars(primaryAddress.getAddressLine1()) || hasWelshChars(primaryAddress.getAddressLine2())
            || hasWelshChars(primaryAddress.getAddressLine3()) || hasWelshChars(primaryAddress.getPostTown())
            || hasWelshChars(primaryAddress.getCounty()) || hasWelshChars(primaryAddress.getPostCode())) {
            errors.add(WELSH_CHAR_ERROR);
        }
        return errors;
    }

    public List<String> validateName(String partyName, List<String> errors) {

        if (exceedsLength(partyName, NAME_MAX_LENGTH_ALLOWED)) {
            errors.add(NAME_LENGTH_ERROR);
        }
        return errors;
    }

    private boolean hasWelshChars(String strToMatch) {
        for (String s : WELSH_CHARS) {
            if (strToMatch != null && strToMatch.indexOf(s) != -1) {
                return true;
            }
        }
        return false;
    }

    private boolean exceedsLength(String strToCheck, int length) {
        return strToCheck != null && strToCheck.length() > length ? true : false;
    }
}
