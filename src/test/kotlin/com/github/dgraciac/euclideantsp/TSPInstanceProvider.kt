package com.github.dgraciac.euclideantsp

import java.util.stream.Stream
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider

class TSPInstanceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<Arguments> {
        return Stream.of(
            Arguments.of(TRIVIAL),
            Arguments.of(_4_SQUARE),
            Arguments.of(_4_A),
            Arguments.of(_4_B),
            Arguments.of(_5_A),
            Arguments.of(_5_B),
            Arguments.of(_6_A),
            Arguments.of(_6_B),
            Arguments.of(_6_C),
            Arguments.of(_6_D),
            Arguments.of(_10_A),
            Arguments.of(BERLIN_52),
            Arguments.of(ST_70)
//            ,
//            Arguments.of(KRO_200),
//            Arguments.of(A_280)
        )
    }
}
