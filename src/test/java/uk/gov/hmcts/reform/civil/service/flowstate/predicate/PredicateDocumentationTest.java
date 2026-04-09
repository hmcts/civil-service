package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

class PredicateDocumentationTest {

    @Test
    void ensureAllPredicatesAreDocumented() {
        List<String> undocumentedMembers = new ArrayList<>();

        List<Class<?>> containers = List.of(
            CaseDataPredicate.class,
            ClaimantPredicate.class,
            ClaimPredicate.class,
            DismissedPredicate.class,
            DivergencePredicate.class,
            HearingPredicate.class,
            LanguagePredicate.class,
            LipPredicate.class,
            NotificationPredicate.class,
            OutOfTimePredicate.class,
            PaymentPredicate.class,
            RepaymentPredicate.class,
            ResponsePredicate.class,
            TakenOfflinePredicate.class
        );

        for (Class<?> container : containers) {
            for (Class<?> clazz : container.getClasses()) {
                // Check fields
                for (Field field : clazz.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())
                        && field.getType().equals(Predicate.class)
                        && !field.isAnnotationPresent(BusinessRule.class)) {
                        undocumentedMembers.add(clazz.getSimpleName() + "." + field.getName());
                    }
                }
                // Check methods
                for (Method method : clazz.getDeclaredMethods()) {
                    if (Modifier.isStatic(method.getModifiers())
                        && method.getReturnType().equals(Predicate.class)
                        && !method.isAnnotationPresent(BusinessRule.class)) {
                        undocumentedMembers.add(clazz.getSimpleName() + "." + method.getName() + "()");
                    }
                }
            }
        }

        // ensure test has an assertion for test frameworks and reporting
        Assertions.assertTrue(undocumentedMembers.isEmpty(),
            "The following predicates are missing @BusinessRule documentation:\n" + String.join("\n", undocumentedMembers));
    }

    @Disabled("Used to manually generate business rule documentation")
    @Test
    void generateMarkdownReport() throws Exception {
        StringBuilder md = new StringBuilder();
        md.append("# Civil Service Business Rules\n\n");
        md.append("> This file is auto-generated from the source code. Do not edit manually.\n\n");

        // --- Composed predicates (Composer) ---
        md.append("## Composed predicates (Business Rules)\n\n");
        md.append("| Group | Rule Name | Logic Description |\n");
        md.append("|---|---|---|\n");

        List<Object> composedMembers = new ArrayList<>();
        List<Class<?>> composerToScan = List.of(
            ClaimantPredicate.class,
            ClaimPredicate.class,
            DismissedPredicate.class,
            DivergencePredicate.class,
            HearingPredicate.class,
            LanguagePredicate.class,
            LipPredicate.class,
            NotificationPredicate.class,
            OutOfTimePredicate.class,
            PaymentPredicate.class,
            RepaymentPredicate.class,
            ResponsePredicate.class,
            TakenOfflinePredicate.class
        );

        for (Class<?> clazz : composerToScan) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                    && field.getType().equals(Predicate.class)
                    && field.isAnnotationPresent(BusinessRule.class)) {
                    composedMembers.add(field);
                }
            }
            for (Method method : clazz.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())
                    && method.getReturnType().equals(Predicate.class)
                    && method.isAnnotationPresent(BusinessRule.class)) {
                    composedMembers.add(method);
                }
            }
        }

        composedMembers.sort(Comparator.comparing(member -> {
            if (member instanceof Field) {
                return ((Field) member).getAnnotation(BusinessRule.class).group();
            }
            return ((Method) member).getAnnotation(BusinessRule.class).group();
        }).thenComparing(member -> {
            if (member instanceof Field) {
                return ((Field) member).getName();
            }
            return ((Method) member).getName();
        }));

        for (Object member : composedMembers) {
            if (member instanceof Field field) {
                BusinessRule rule = field.getAnnotation(BusinessRule.class);
                md.append(String.format("| **%s** | `%s` | %s |%n",
                                        rule.group(),
                                        field.getName(),
                                        rule.description()
                ));
            } else {
                Method method = (Method) member;
                BusinessRule rule = method.getAnnotation(BusinessRule.class);
                md.append(String.format("| **%s** | `%s()` | %s |%n",
                                        rule.group(),
                                        method.getName(),
                                        rule.description()
                ));
            }
        }

        md.append("\n---\n\n");

        // --- Atomic predicates (CaseDataPredicate) ---
        md.append("## Atomic predicates (CaseData Predicates)\n\n");
        md.append("| Group | Rule Name | Logic Description |\n");
        md.append("|---|---|---|\n");

        List<Class<?>> classesToScan = new ArrayList<>();
        classesToScan.add(CaseDataPredicate.class);
        classesToScan.addAll(List.of(CaseDataPredicate.class.getClasses()));

        List<Object> atomicMembers = new ArrayList<>();
        for (Class<?> clazz : classesToScan) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                    && field.getType().equals(Predicate.class)
                    && field.isAnnotationPresent(BusinessRule.class)) {
                    atomicMembers.add(field);
                }
            }
            for (Method method : clazz.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers())
                    && method.getReturnType().equals(Predicate.class)
                    && method.isAnnotationPresent(BusinessRule.class)) {
                    atomicMembers.add(method);
                }
            }
        }

        atomicMembers.sort(Comparator.comparing(member -> {
            if (member instanceof Field) {
                return ((Field) member).getAnnotation(BusinessRule.class).group();
            }
            return ((Method) member).getAnnotation(BusinessRule.class).group();
        }).thenComparing(member -> {
            if (member instanceof Field) {
                return ((Field) member).getName();
            }
            return ((Method) member).getName();
        }));

        for (Object member : atomicMembers) {
            if (member instanceof Field field) {
                BusinessRule rule = field.getAnnotation(BusinessRule.class);
                md.append(String.format("| **%s** | `%s` | %s |%n",
                                        rule.group(),
                                        field.getName(),
                                        rule.description()
                ));
            } else {
                Method method = (Method) member;
                BusinessRule rule = method.getAnnotation(BusinessRule.class);
                md.append(String.format("| **%s** | `%s()` | %s |%n",
                                        rule.group(),
                                        method.getName(),
                                        rule.description()
                ));
            }
        }

        File targetDir = new File("docs");
        boolean buildDirOk = targetDir.exists() || targetDir.mkdirs();
        Assertions.assertTrue(buildDirOk, "Failed to create build directory");

        File outputFile = new File(targetDir, "business-rules.md");
        Files.writeString(outputFile.toPath(), md.toString());

        System.out.println("Documentation generated at: " + outputFile.getAbsolutePath());

        Assertions.assertTrue(outputFile.exists());
    }
}
