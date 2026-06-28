package it.fulminazzo.creeper.tester;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * General DTO class to report tests results.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
abstract class TestResult {
    boolean success = true;

    /**
     * DTO test result to report successful tests execution.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    @Builder
    static final class SuccessfulTestResult extends TestResult {
        long timeStarted;
        long timeFinished;

        long failedContainers;
        long succeededContainers;
        long skippedContainers;
        long totalContainers;
        @NotNull List<Failure> containerFailures;

        long failedTests;
        long succeededTests;
        long skippedTests;
        long totalTests;
        @NotNull List<Failure> testFailures;

        /**
         * Generates a {@link SuccessfulTestResult} from a JUnit {@link TestExecutionSummary}.
         *
         * @param summary the summary to generate the data from
         * @return the test results
         */
        public static @NotNull TestResult.SuccessfulTestResult of(final @NotNull TestExecutionSummary summary) {
            return SuccessfulTestResult.builder()
                    .timeStarted(summary.getTimeStarted())
                    .timeFinished(summary.getTimeFinished())
                    .failedContainers(summary.getContainersFailedCount())
                    .succeededContainers(summary.getContainersSucceededCount())
                    .skippedContainers(summary.getContainersSkippedCount())
                    .totalContainers(summary.getContainersFoundCount())
                    .containerFailures(summary.getFailures().stream()
                            .filter(f -> f.getTestIdentifier().isContainer())
                            .map(Failure::of)
                            .collect(Collectors.toList())
                    )
                    .failedTests(summary.getTestsFailedCount())
                    .succeededTests(summary.getTestsSucceededCount())
                    .skippedTests(summary.getTestsSkippedCount())
                    .totalTests(summary.getTestsFoundCount())
                    .testFailures(summary.getFailures().stream()
                            .filter(f -> f.getTestIdentifier().isTest())
                            .map(Failure::of)
                            .collect(Collectors.toList())
                    )
                    .build();
        }

    }

    /**
     * Contains all relevant information about a failure, either test or container.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static final class Failure {
        @Nullable String displayName;
        @Nullable String message;
        @Nullable TestSource source;
        @NotNull ThrowableData exception;

        /**
         * Extracts a {@link Failure} from a JUnit {@link TestExecutionSummary.Failure}.
         *
         * @param failure the failure to extract the data from
         * @return the failure data
         */
        public static @NotNull Failure of(final @NotNull TestExecutionSummary.Failure failure) {
            final TestIdentifier identifier = failure.getTestIdentifier();
            final Throwable exception = failure.getException();
            TestSource source = TestSource.of(identifier);
            return new Failure(identifier.getDisplayName(), exception.getMessage(), source, ThrowableData.of(exception));
        }

    }

    /**
     * Contains all relevant information about a test source (either class or method).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    static final class TestSource {
        @NotNull String className;
        @Nullable String methodName;
        @Nullable String methodParameters;

        /**
         * Extracts the {@link TestSource} from a JUnit {@link TestIdentifier}.
         *
         * @param identifier the identifier to get the source from
         * @return {@code null} if there is no source or is not recognized
         */
        public static @Nullable TestSource of(final @NotNull TestIdentifier identifier) {
            return identifier.getSource().map(source -> {
                if (source instanceof MethodSource) {
                    MethodSource methodSource = (MethodSource) source;
                    return new TestSource(methodSource.getClassName(), methodSource.getMethodName(), methodSource.getMethodParameterTypes());
                } else if (source instanceof ClassSource) {
                    ClassSource classSource = (ClassSource) source;
                    return new TestSource(classSource.getClassName(), null, null);
                } else return null;
            }).orElse(null);
        }
    }

    /**
     * DTO test result to report exceptions while preparing the environment for tests execution.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    static final class ThrowableResult extends TestResult {
        @NotNull ThrowableData exception;

        /**
         * Instantiates a new Throwable result.
         *
         * @param exception the exception that caused the failure
         */
        public ThrowableResult(final @NotNull Throwable exception) {
            setSuccess(false);
            this.exception = ThrowableData.of(exception);
        }

    }

    /**
     * Contains all relevant information about a {@link Throwable}.
     */
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    static final class ThrowableData {
        @NotNull String throwableName;
        @Nullable String message;
        @NotNull List<String> stackTrace;
        @Nullable ThrowableData cause;

        /**
         * Generates an {@link ThrowableData} from a {@link Throwable}.
         *
         * @param throwable the throwable to generate the data from
         * @return the throwable data
         */
        public static @NotNull ThrowableData of(final @NotNull Throwable throwable) {
            Throwable cause = throwable.getCause();
            ThrowableData causeData = cause != null ? of(cause) : null;
            return new ThrowableData(
                    throwable.getClass().getCanonicalName(),
                    throwable.getMessage(),
                    Arrays.stream(throwable.getStackTrace()).map(Object::toString).collect(Collectors.toList()),
                    causeData
            );
        }

    }

}
