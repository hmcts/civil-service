package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
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
            addressLines.add(originalAddress.getPostTown());
            return splitBySpace(originalAddress, addressLines);
        } else {
            return originalAddress.toBuilder()
                .addressLine1(Iterables.get(addressLines, 0, null))
                .addressLine2(Iterables.get(addressLines, 1, null))
                .addressLine3(Iterables.get(addressLines, 2, null))
                .build();
        }
    }

    private Address splitBySpace(Address originalAddress, List<String> addressLines) {
        addressLines = addressLines.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        Queue<String> addressParts = new LinkedList<>();

        String addressLine1 = getAddressLine(addressLines, addressParts, 0);
        String addressLine2 = getAddressLine(addressLines, addressParts, 1);
        String addressLine3 = getAddressLine(addressLines, addressParts, 2);
        String postTown = getAddressLine(addressLines, addressParts, 3);

        return originalAddress.toBuilder()
            .addressLine1(StringUtils.isEmpty(addressLine1) ? null : addressLine1)
            .addressLine2(StringUtils.isEmpty(addressLine2) ? null : addressLine2)
            .addressLine3(StringUtils.isEmpty(addressLine3) ? null : addressLine3)
            .postTown(StringUtils.isEmpty(postTown) ? null : postTown)
            .build();
    }

    @Nullable
    private String getAddressLine(List<String> addressLines, Queue<String> addressParts, int position) {
        String srcAddressLine = Iterables.get(addressLines, position, "");
        String addressLine = "";
        if (addressParts.size() > 0 && StringUtils.isNotEmpty(srcAddressLine)) {
            addressParts.offer(",");
        }
        String addressLineWithCarryover = String.join(" ", addressParts).concat(srcAddressLine);
        if (StringUtils.length(addressLineWithCarryover) > LINE_LIMIT) {
            if (StringUtils.isNotEmpty(srcAddressLine)) {
                addressParts.addAll(Splitter.on(' ').omitEmptyStrings().splitToList(srcAddressLine));
            }
            String delimiter = "";
            while (addressLine.concat(delimiter).concat(ofNullable(addressParts.peek()).orElse(""))
                .length() < LINE_LIMIT) {
                addressLine = addressLine.concat(delimiter).concat(requireNonNull(addressParts.poll()));
                delimiter = " ";
            }
            if (StringUtils.isEmpty(addressLine)) {
                addressLine = addressLineWithCarryover;
                addressParts.clear();
            }
        } else {
            addressLine = addressLineWithCarryover;
        }
        return position == 3 && addressLines.size() > 4 ? addressLine.concat(", ").concat(String.join(",",
            addressLines.subList(4, addressLines.size()))) : addressLine;
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
