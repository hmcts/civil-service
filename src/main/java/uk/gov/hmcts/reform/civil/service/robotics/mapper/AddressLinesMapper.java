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
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Component
public class AddressLinesMapper {

    private static final int LINE_LIMIT = 35;
    private static final String STRING_EMPTY = "";
    private static final String STRING_SPACE = " ";
    private static final String STRING_COMMA = ",";
    private static final String STRING_COMMA_SPACE = ", ";
    private static final char CHAR_COMMA = ',';
    private static final char CHAR_SPACE = ' ';

    public Address splitLongerLines(Address originalAddress) {
        requireNonNull(originalAddress);

        List<String> addressLines = prepareAddressLines(originalAddress);
        boolean anyLineExceedsLimit = addressLines.stream().anyMatch(line -> line.length() > LINE_LIMIT);
        if (addressLines.size() > 3 || anyLineExceedsLimit) {
            return resolveAddressBySpace(originalAddress);
        } else {
            originalAddress.setAddressLine1(Iterables.get(addressLines, 0, null));
            originalAddress.setAddressLine2(Iterables.get(addressLines, 1, null));
            originalAddress.setAddressLine3(Iterables.get(addressLines, 2, null));
            return originalAddress;
        }
    }

    private Address resolveAddressBySpace(Address originalAddress) {
        Queue<String> addressParts = resolveAddressLine(originalAddress.getAddressLine1(), STRING_EMPTY, true);
        String addressLine1 = addressParts.poll();
        originalAddress.setAddressLine1(StringUtils.isEmpty(addressLine1) ? null : addressLine1);

        addressParts = resolveAddressLine(originalAddress.getAddressLine2(), addressParts.poll(), true);
        String addressLine2 = addressParts.poll();
        originalAddress.setAddressLine2(StringUtils.isEmpty(addressLine2) ? null : addressLine2);

        addressParts = resolveAddressLine(originalAddress.getAddressLine3(), addressParts.poll(), true);
        String addressLine3 = addressParts.poll();
        originalAddress.setAddressLine3(StringUtils.isEmpty(addressLine3) ? null : addressLine3);

        addressParts = resolveAddressLine(originalAddress.getPostTown(), addressParts.poll(), false);
        String postTown = addressParts.poll();
        originalAddress.setPostTown(StringUtils.isEmpty(postTown) ? null : postTown);

        return originalAddress;
    }

    private Queue<String> resolveAddressLine(String addressLine, String overflow, boolean overflowAllowed) {
        String normalisedAddressLine = StringUtils.defaultString(addressLine);
        Optional<Queue<String>> emptyResolution = resolveWhenAddressLineEmpty(normalisedAddressLine, overflow);
        if (emptyResolution.isPresent()) {
            return emptyResolution.get();
        }
        if (!overflowAllowed) {
            return resolveWithoutOverflowAllowance(normalisedAddressLine, overflow);
        }
        return resolveWithOverflowAllowance(normalisedAddressLine, overflow);
    }

    private Optional<Queue<String>> resolveWhenAddressLineEmpty(String addressLine, String overflow) {
        if (StringUtils.isEmpty(addressLine)) {
            if (StringUtils.isEmpty(overflow)) {
                return Optional.of(new LinkedList<>(Arrays.asList(null, STRING_EMPTY)));
            }
            if (StringUtils.length(overflow) <= LINE_LIMIT) {
                return Optional.of(new LinkedList<>(List.of(overflow, STRING_EMPTY)));
            }
        }
        return Optional.empty();
    }

    private Queue<String> resolveWithoutOverflowAllowance(String addressLine, String overflow) {
        String safeAddressLine = Objects.requireNonNullElse(addressLine, STRING_EMPTY);
        int addLineLen = StringUtils.length(safeAddressLine);
        String addressLineCandidate = StringUtils.defaultString(overflow).concat(safeAddressLine);
        String returnAddress;
        if (addressLineCandidate.length() > LINE_LIMIT) {
            if (addLineLen >= LINE_LIMIT) {
                returnAddress = safeAddressLine.substring(0, Math.min(safeAddressLine.length(), LINE_LIMIT - 1));
            } else {
                String overflowSubstring = overflow != null
                    ? overflow.substring(0, Math.max(0, LINE_LIMIT - 3 - addLineLen)) : STRING_EMPTY;
                returnAddress = overflowSubstring.concat(STRING_COMMA_SPACE).concat(safeAddressLine);
            }
        } else {
            returnAddress = addressLineCandidate;
        }
        return new LinkedList<>(Arrays.asList(returnAddress, STRING_EMPTY));
    }

    private Queue<String> resolveWithOverflowAllowance(String addressLine, String overflow) {
        String safeAddressLine = StringUtils.defaultString(addressLine);
        String normalisedOverflow = overflow == null ? STRING_EMPTY : overflow;
        if (StringUtils.length(normalisedOverflow) + StringUtils.length(safeAddressLine) > LINE_LIMIT) {
            return splitOverflowingAddress(safeAddressLine, normalisedOverflow);
        }
        String retained = normalisedOverflow.concat(safeAddressLine);
        return new LinkedList<>(List.of(retained.trim(), STRING_EMPTY));
    }

    private Queue<String> splitOverflowingAddress(String addressLine, String overflow) {
        String safeAddressLine = StringUtils.defaultString(addressLine);
        String safeOverflow = StringUtils.defaultString(overflow);
        String retained = STRING_EMPTY;
        Queue<String> addressParts = new LinkedList<>(Splitter.on(CHAR_SPACE).omitEmptyStrings()
                                                          .splitToList(safeOverflow.concat(safeAddressLine)));
        while (!addressParts.isEmpty() && retained.concat(STRING_SPACE).concat(addressParts.peek()).length() <= LINE_LIMIT) {
            retained = retained.concat(STRING_SPACE).concat(requireNonNull(addressParts.poll()));
        }
        String processedOverflow;
        if (StringUtils.isEmpty(retained)) {
            retained = safeAddressLine;
            processedOverflow = STRING_EMPTY;
        } else {
            String overflowStr = String.join(STRING_SPACE, addressParts);
            processedOverflow = overflowStr.concat(overflowStr.trim().endsWith(STRING_COMMA)
                                                  ? STRING_SPACE : STRING_COMMA_SPACE);
        }
        return new LinkedList<>(List.of(retained.trim(), processedOverflow));
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
            .stream().map(String::trim).toList();
    }
}
