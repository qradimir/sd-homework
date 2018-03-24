package ru.itmo.ctddev.sorokin.sd.aop.profiler

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Around

const val PACKAGE = "ru.itmo.ctddev.sorokin.sd.aop.demo"

@Aspect
class DemoPackageProfilerAspect {

    @Around("call(* ${PACKAGE}..*(..))")
    fun profile(joinPoint: ProceedingJoinPoint) = Profiler.profile(joinPoint)

    @After("execution(* ${PACKAGE}.DemoMethodsKt.main(..))")
    fun reportReport() = ProfileReporter.report()
}