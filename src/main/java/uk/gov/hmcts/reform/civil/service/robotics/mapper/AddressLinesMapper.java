package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Component
public class AddressLinesMapper {

    private static final int LINE_LIMIT = 35;
    private static final char CHAR_COMMA = ',';

    public Address splitLongerLines(Address originalAddress) {
        requireNonNull(originalAddress);

        List<String> addressLines = prepareAddressLines(originalAddress);
        boolean anyLineExceedsLimit = addressLines.stream().anyMatch(line -> line.length() > LINE_LIMIT);
        if (addressLines.size() > 3 || anyLineExceedsLimit) {
            return appendLinesWithinLineLimit(originalAddress, addressLines);
        } else {
            return originalAddress.toBuilder()
                .addressLine1(Iterables.get(addressLines, 0, null))
                .addressLine2(Iterables.get(addressLines, 1, null))
                .addressLine3(Iterables.get(addressLines, 2, null))
                .build();
        }
    }

    private Address appendLinesWithinLineLimit(Address originalAddress, List<String> addressLines) {
        addressLines = addressLines.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        String curAddressLine1 = Iterables.get(addressLines, 0, "");
        String curAddressLine2 = Iterables.get(addressLines, 1, "");
        String curAddressLine3 = Iterables.get(addressLines, 2, "");
        String curAddressLine4 = Iterables.get(addressLines, 3, "");
        String addressLine1 = "";
        String addressLine2 = "";
        String addressLine3 = "";
        String postTown = "";
        Queue<String> addressParts = new LinkedList<>();

        if (StringUtils.length(curAddressLine1) > LINE_LIMIT) {
            if (StringUtils.isNotEmpty(curAddressLine1)) {
                addressParts.addAll(Splitter.on(' ').omitEmptyStrings().splitToList(curAddressLine1));
            }
            String delimiter = "";
            while (addressLine1.concat(delimiter).concat(ofNullable(addressParts.peek()).orElse(""))
                .length() < LINE_LIMIT) {
                addressLine1 = addressLine1.concat(delimiter).concat(requireNonNull(addressParts.poll()));
                delimiter = " ";
            }
            if (StringUtils.isEmpty(addressLine1)) {
                addressLine1 = curAddressLine1;
            }
        } else {
            addressLine1 = String.join(" ", addressParts).concat(curAddressLine1);
        }

        if (addressParts.size() > 0 && StringUtils.isNotEmpty(curAddressLine2)) {
            addressParts.offer(",");
        }
        if (String.join(" ", addressParts).length() +  StringUtils.length(curAddressLine2) > LINE_LIMIT) {
            if (StringUtils.isNotEmpty(curAddressLine2)) {
                addressParts.addAll(Splitter.on(' ').omitEmptyStrings().splitToList(curAddressLine2));
            }
            String delimiter = "";
            while (addressLine2.concat(delimiter).concat(ofNullable(addressParts.peek()).orElse(""))
                .length() < LINE_LIMIT) {
                addressLine2 = addressLine2.concat(delimiter).concat(requireNonNull(addressParts.poll()));
                delimiter = " ";
            }
            if (StringUtils.isEmpty(addressLine2)) {
                addressLine2 = curAddressLine2;
            }
        } else {
            addressLine2 = String.join(" ", addressParts).concat(curAddressLine2);
        }

        if (addressParts.size() > 0 && StringUtils.isNotEmpty(curAddressLine3)) {
            addressParts.offer(",");
        }
        if (String.join(" ", addressParts).length() +  StringUtils.length(curAddressLine3) > LINE_LIMIT) {
            if (StringUtils.isNotEmpty(curAddressLine3)) {
                addressParts.addAll(Splitter.on(' ').omitEmptyStrings().splitToList(curAddressLine3));
            }
            String delimiter = "";
            while (addressLine3.concat(delimiter).concat(ofNullable(addressParts.peek()).orElse(""))
                .length() < LINE_LIMIT) {
                addressLine3 = addressLine3.concat(delimiter).concat(requireNonNull(addressParts.poll()));
                delimiter = " ";
            }
            if (StringUtils.isEmpty(addressLine3)) {
                addressLine3 = curAddressLine3;
            }
        } else {
            addressLine3 = String.join(" ", addressParts).concat(curAddressLine3);
        }

        if (addressParts.size() > 0 && StringUtils.isNotEmpty(curAddressLine4)) {
            addressParts.offer(",");
        }
        if (String.join(" ", addressParts).length() +  StringUtils.length(curAddressLine4) > LINE_LIMIT) {
            if (StringUtils.isNotEmpty(curAddressLine4)) {
                addressParts.addAll(Splitter.on(' ').omitEmptyStrings().splitToList(curAddressLine4));
            }
            String delimiter = "";
            while (postTown.concat(delimiter).concat(ofNullable(addressParts.peek()).orElse(""))
                .length() < LINE_LIMIT) {
                postTown = postTown.concat(delimiter).concat(requireNonNull(addressParts.poll()));
                delimiter = " ";
            }
            if (StringUtils.isEmpty(postTown)) {
                postTown = curAddressLine4;
            }
        } else {
            postTown = String.join(" ", addressParts).concat(curAddressLine4);
        }

        return originalAddress.toBuilder()
            .addressLine1(StringUtils.isEmpty(addressLine1) ? null : addressLine1)
            .addressLine2(StringUtils.isEmpty(addressLine2) ? null : addressLine2)
            .addressLine3(StringUtils.isEmpty(addressLine3) ? null : addressLine3)
            .postTown(StringUtils.isEmpty(postTown) ? null : postTown)
            .build();
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
        return line.length() > LINE_LIMIT ? splitByLastCharacter(line, CHAR_COMMA) : asList(line);
    }

    private List<String> splitByLastCharacter(String line, Character ch) {
        List<String> tokens = new ArrayList<>(Splitter.on(ch)
                                                  .omitEmptyStrings()
                                                  .splitToList(line));
        String lastToken = tokens.remove(tokens.size() - 1);
        return asList(String.join(",", tokens), lastToken.trim())
            .stream().map(String::trim).collect(Collectors.toList());
    }
}
