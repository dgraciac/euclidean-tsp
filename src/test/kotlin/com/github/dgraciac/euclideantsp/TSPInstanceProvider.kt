package com.github.dgraciac.euclideantsp

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class TSPInstanceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
        Stream.of(
/*            Arguments.of(TRIVIAL),
            Arguments.of(instance4Square),
            Arguments.of(instance4A),
            Arguments.of(instance4B),
            Arguments.of(instance5A),
            Arguments.of(instance5B),
            Arguments.of(instance6A),
            Arguments.of(instance6B),
            Arguments.of(instance6C),
            Arguments.of(instance6D)
            ,
            Arguments.of(instance10A),*/
            Arguments.of(EIL_51),
            Arguments.of(BERLIN_52),
            Arguments.of(ST_70),
            Arguments.of(EIL_76),
            Arguments.of(RAT_99),
            Arguments.of(KRO_200),
            Arguments.of(A_280),
        )
}
