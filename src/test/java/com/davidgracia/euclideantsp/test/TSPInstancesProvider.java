package com.davidgracia.euclideantsp.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class TSPInstancesProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(Euclidean2DTSPInstances.INSTANCE_1),
                Arguments.of(Euclidean2DTSPInstances.INSTANCE_2),
                Arguments.of(Euclidean2DTSPInstances.INSTANCE_3),
                Arguments.of(Euclidean2DTSPInstances.INSTANCE_4),
                Arguments.of(Euclidean2DTSPInstances.INSTANCE_5),
                Arguments.of(Euclidean2DTSPInstances.INSTANCE_6)
        );
    }
}
