package ru.itmo.ctddev.sorokin.sd.aop.profiler

import org.aspectj.lang.ProceedingJoinPoint

typealias ProfilerInfo = HashMap<String, ProfileData>

class ProfileData(
        var fullTime: Long = 0,
        var count: Int = 0,
        var nestedCalls: ProfilerInfo = ProfilerInfo()
)

class Profiler(val timer: ()->Long) {

    val rootProfileInfo: ProfilerInfo = ProfilerInfo()

    var currentProfileData: ProfileData? = null

    inline fun <R> profile(methodName: String, body: () -> R): R {
        val profileInfo = currentProfileData?.nestedCalls ?: rootProfileInfo
        val enclosingProfileData = currentProfileData
        val profileData = profileInfo.computeIfAbsent(methodName) { ProfileData() }
        currentProfileData = profileData

        val start = timer()
        val result = body()
        val time = timer() - start

        profileData.fullTime += time
        profileData.count += 1
        currentProfileData = enclosingProfileData
        return result
    }

    fun profile(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature
        val methodName = signature.declaringTypeName + "." + signature.name
        return profile<Any?>(methodName) { joinPoint.proceed() }
    }


    companion object {
        val INSTANCE = Profiler { System.nanoTime() }

        fun profile(joinPoint: ProceedingJoinPoint) = INSTANCE.profile(joinPoint)
    }
}

fun ProfilerInfo.flatten() = ProfilerInfo().also { this.computeFlatten(it) }

private fun ProfilerInfo.computeFlatten(flattenProfilerInfo: ProfilerInfo) {
    for ((key, value) in entries) {
        val flattenValue = flattenProfilerInfo.computeIfAbsent(key) { ProfileData() }
        flattenValue.count += value.count
        flattenValue.fullTime += value.fullTime
        value.nestedCalls.computeFlatten(flattenProfilerInfo)
    }
}