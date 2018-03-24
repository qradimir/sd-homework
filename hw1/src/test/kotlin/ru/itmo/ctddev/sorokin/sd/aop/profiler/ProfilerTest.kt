package ru.itmo.ctddev.sorokin.sd.aop.profiler

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("RemoveRedundantBackticks")
class ProfilerTest {

    lateinit var profiler: Profiler

    companion object {
        var time: Long = 0

        fun teak() {
            time += 1
        }
    }

    @Before
    fun init() {
        profiler = Profiler { time }
    }

    @Test
    fun `single method`() {
        profiler.profile("method") { teak() }

        assertProfile(hashMapOf("method" to ProfileData(1, 1)))
    }

    @Test
    fun `nested method`() {
        profiler.profile("method1") {
            profiler.profile("method2") { teak() }
        }

        assertProfile(hashMapOf("method1" to ProfileData(1, 1, hashMapOf("method2" to ProfileData(1, 1)))))
    }

    @Test
    fun `several methods`() {
        profiler.profile("method1") { teak() }
        profiler.profile("method2") { teak() }

        assertProfile(hashMapOf("method1" to ProfileData(1, 1), "method2" to ProfileData(1, 1)))
    }

    @Test
    fun `several call-sites`() {
        profiler.profile("method1") {
            profiler.profile("method2") { teak() }
        }
        profiler.profile("method2") { teak() }

        assertProfile(hashMapOf(
                "method1" to ProfileData(1, 1, hashMapOf("method2" to ProfileData(1, 1))),
                "method2" to ProfileData(1, 1)
        ))
        assertFlattenProfile(hashMapOf(
                "method1" to ProfileData(1, 1),
                "method2" to ProfileData(2, 2)
        ))
    }

    @Test
    fun `recursion`() {
        profiler.profile("method") {
            teak()
            profiler.profile("method") {
                teak()
                profiler.profile("method") {
                    teak()
                }
            }
        }

        assertProfile(hashMapOf("method" to ProfileData(3, 1,
                hashMapOf("method" to ProfileData(2, 1,
                        hashMapOf("method" to ProfileData(1, 1)))))
        ))
        assertFlattenProfile(hashMapOf("method" to ProfileData(6, 3)))
    }

    fun assertProfileSame(expectedProfilerInfo: ProfilerInfo, actualProfilerInfo: ProfilerInfo) {
        assertEquals(expectedProfilerInfo.size, actualProfilerInfo.size)
        for ((expectedMethodName, expecteData) in expectedProfilerInfo) {
            val actualData = assertNotNull(actualProfilerInfo[expectedMethodName])
            assertEquals(expecteData.count, actualData.count)
            assertEquals(expecteData.fullTime, actualData.fullTime)
            assertProfileSame(expecteData.nestedCalls, actualData.nestedCalls)
        }
    }

    fun assertProfile(expected: ProfilerInfo) = assertProfileSame(expected, profiler.rootProfileInfo)
    fun assertFlattenProfile(expected: ProfilerInfo) = assertProfileSame(expected, profiler.rootProfileInfo.flatten())

}
