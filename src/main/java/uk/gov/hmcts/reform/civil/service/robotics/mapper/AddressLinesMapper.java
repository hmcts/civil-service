package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Address;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String STRING_EMPTY = "";
    private static final String STRING_SPACE = " ";
    private static final char CHAR_COMMA = ',';
    private static final char CHAR_SPACE = ' ';

    public Address splitLongerLines(Address originalAddress) {
        requireNonNull(originalAddress);

        List<String> addressLines = prepareAddressLines(originalAddress);
        boolean anyLineExceedsLimit = addressLines.stream().anyMatch(line -> line.length() > LINE_LIMIT);
        if (addressLines.size() > 3 || anyLineExceedsLimit) {
            return resolveAddressBySpace(originalAddress);
        } else {
            return originalAddress.toBuilder()
                .addressLine1(Iterables.get(addressLines, 0, null))
                .addressLine2(Iterables.get(addressLines, 1, null))
                .addressLine3(Iterables.get(addressLines, 2, null))
                .build();
        }
    }

    private Address resolveAddressBySpace(Address originalAddress) {
        Address.AddressBuilder addressBuilder = originalAddress.toBuilder();

        Queue<String> addressParts = resolveAddressLine(originalAddress.getAddressLine1(), STRING_EMPTY, true);
        String addressLine1 = addressParts.poll();
        addressBuilder.addressLine1(StringUtils.isEmpty(addressLine1) ? null : addressLine1);

        addressParts = resolveAddressLine(originalAddress.getAddressLine2(), addressParts.poll(), true);
        String addressLine2 = addressParts.poll();
        addressBuilder.addressLine2(StringUtils.isEmpty(addressLine2) ? null : addressLine2);

        addressParts = resolveAddressLine(originalAddress.getAddressLine3(), addressParts.poll(), true);
        String addressLine3 = addressParts.poll();
        addressBuilder.addressLine3(StringUtils.isEmpty(addressLine3) ? null : addressLine3);

        addressParts = resolveAddressLine(originalAddress.getPostTown(), addressParts.poll(), false);
        String postTown = addressParts.poll();
        addressBuilder.postTown(StringUtils.isEmpty(postTown) ? null : postTown);

        return addressBuilder.build();
    }

    private Queue<String> resolveAddressLine(String addressLine, String overflow, boolean overflowAllowed) {
        String retained;
        addressLine = StringUtils.normalizeSpace(addressLine);
        if (StringUtils.isEmpty(addressLine)) {
            if (StringUtils.isEmpty(overflow)) {
                return new LinkedList<>(Arrays.asList(null, null));
            } else if (StringUtils.length(overflow) <= LINE_LIMIT) {
                return new LinkedList<>(List.of(overflow, overflow));
            }
        }
        if (!overflowAllowed) {
            String returnAddress = StringUtils.length(overflow.concat(ofNullable(addressLine).orElse("")))
                > LINE_LIMIT ? addressLine : overflow.concat(ofNullable(addressLine).orElse(""));
            return new LinkedList<>(Arrays.asList(returnAddress, STRING_EMPTY));
        }
        if (StringUtils.length(overflow) + StringUtils.length(addressLine) > LINE_LIMIT) {
            retained = STRING_EMPTY;
            Queue<String> addressParts = new LinkedList<>(Splitter.on(CHAR_SPACE).omitEmptyStrings()
                .splitToList(overflow.concat(ofNullable(addressLine).orElse(""))));
            while (addressParts.isEmpty() || retained.concat(" ").concat(addressParts.peek()).length() <= LINE_LIMIT) {
                retained = retained.concat(STRING_SPACE).concat(requireNonNull(addressParts.poll()));
            }
            if (StringUtils.isEmpty(retained)) {
                retained = addressLine;
                overflow = STRING_EMPTY;
            } else {
                String overflowStr = String.join(STRING_SPACE, addressParts);
                overflow = overflowStr.concat(overflowStr.trim().endsWith(",") ? " " : ", ");
            }
        } else {
            retained = addressLine;
            overflow = STRING_EMPTY;
        }
        return new LinkedList<>(List.of(retained.trim(), overflow));
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
