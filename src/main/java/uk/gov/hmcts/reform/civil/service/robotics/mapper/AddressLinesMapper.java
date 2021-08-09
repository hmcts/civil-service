package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Component
public class AddressLinesMapper {

    private static final int LINE_LIMIT = 35;

    public Address splitLongerLines(Address originalAddress) {
        requireNonNull(originalAddress);

        List<String> addressLines = prepareAddressLines(originalAddress);
        boolean anyLineExceedsLimit = addressLines.stream().anyMatch(line -> line.length() > LINE_LIMIT);
        if (addressLines.size() > 3 || anyLineExceedsLimit) {
            return originalAddress;
        } else {
            return originalAddress.toBuilder()
                .addressLine1(Iterables.get(addressLines, 0, null))
                .addressLine2(Iterables.get(addressLines, 1, null))
                .addressLine3(Iterables.get(addressLines, 2, null))
                .build();
        }
    }

    private List<String> prepareAddressLines(Address originalAddress) {
        List<String> addressLines = new ArrayList<>();
        if (originalAddress.getAddressLine1() != null) {
            addressLines.addAll(splitByCommaIfLongerThanLimit(originalAddress.getAddressLine1()));
        }
        if (originalAddress.getAddressLine2() != null) {
            addressLines.addAll(splitByCommaIfLongerThanLimit(originalAddress.getAddressLine2()));
        }
        if (originalAddress.getAddressLine3() != null) {
            addressLines.addAll(splitByCommaIfLongerThanLimit(originalAddress.getAddressLine3()));
        }
        return addressLines;
    }

    private List<String> splitByCommaIfLongerThanLimit(String line) {
        requireNonNull(line);
        return line.length() > LINE_LIMIT ?
            Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .splitToList(line) :
            asList(line);
    }
}
