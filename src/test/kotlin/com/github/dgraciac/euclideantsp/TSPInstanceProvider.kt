package com.github.dgraciac.euclideantsp

import java.util.stream.Stream
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider

class TSPInstanceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<Arguments> {
        return Stream.of(
            Arguments.of(Euclidean2DTSPInstances.BERLIN_52)
        )
    }
}
