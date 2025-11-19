package lomboksanity;

import lombok.Builder;

@Builder
public class LombokSanityCheck {

    private final int value;

    public static void sanity() {
        LombokSanityCheckBuilder builder = LombokSanityCheck.builder();
        builder.value(42);
        builder.build();
    }
}
