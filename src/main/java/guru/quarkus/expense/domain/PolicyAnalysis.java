package guru.quarkus.expense.domain;

import java.util.List;

public record PolicyAnalysis(
        boolean compliant,
        List<String> violations,
        List<String> missingInformation,
        String explanation
) {
}
