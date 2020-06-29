package com.github.dgraciac.euclideantsp

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class TSPInstancesProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<Arguments> {
        return Stream.of(
            Arguments.of(Euclidean2DTSPInstances.INSTANCE_1),
            Arguments.of(Euclidean2DTSPInstances.INSTANCE_2),
            Arguments.of(Euclidean2DTSPInstances.INSTANCE_3),
            Arguments.of(Euclidean2DTSPInstances.INSTANCE_4),
            Arguments.of(Euclidean2DTSPInstances.INSTANCE_5),
            Arguments.of(Euclidean2DTSPInstances.INSTANCE_6),
            Arguments.of(Euclidean2DTSPInstances.INSTANCE_7)
        )
    }
}