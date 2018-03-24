package ru.itmo.ctddev.sorokin.sd.aop.profiler

import kotlin.math.max

object ProfileReporter {

    fun report() {
        val treeProfileInfo = Profiler.INSTANCE.rootProfileInfo
        val listProfileInfo = Profiler.INSTANCE.rootProfileInfo.flatten()
        println("\nPROFILER REPORT")
        listProfileInfo.report("List view")
        treeProfileInfo.report("Tree view")
        println("\nEND PROFILER REPORT")
    }
}

private fun ProfilerInfo.report(reportName: String) {
    println()
    println(reportName)
    println()
    val methodNameColumnSize = computeMethodNamesColumnSize(indent = 4) + 4
    println("method name${" ".repeat(methodNameColumnSize - 11)}|     count  |  total, s  |    avg, s  |")
    println("${"-".repeat(methodNameColumnSize)}|------------|------------|------------|")
    showTable(methodNameColumnSize, indent = 4)
}



private fun ProfilerInfo.computeMethodNamesColumnSize(indent: Int = 4): Int {
    val thisMax = keys.map { it.length }.max() ?: 0
    val nestedMax = values.map { it.nestedCalls.computeMethodNamesColumnSize(indent) }.max() ?: 0
    return max(thisMax, nestedMax + indent)
}

private fun nanosToSecondsText(ns: Long, presicion: Int = 3): String {
    return "${ns / 1_000_000}.${ns % 1_000_000 / presicions[presicion]}"
}

private fun ProfilerInfo.showTable(nameColumnSize: Int, indent: Int = 0, currentIndentation: Int = 0) {
    val start = " ".repeat(currentIndentation)
    for ((methodName, data) in entries) {
        val end = " ".repeat(nameColumnSize - methodName.length - currentIndentation)

        val countStr = "" + data.count
        val countStart = " ".repeat(10 - countStr.length)

        val totalStr = nanosToSecondsText(data.fullTime)
        val totalStart = " ".repeat(10 - totalStr.length)

        val avgStr = nanosToSecondsText(data.fullTime / data.count)
        val avgStart = " ".repeat(10 - avgStr.length)


        println("$start$methodName$end|$countStart$countStr  |$totalStart$totalStr  |$avgStart$avgStr  |")
        data.nestedCalls.showTable(nameColumnSize, indent, currentIndentation + indent)
    }
}

val presicions = arrayOf(1, 10, 100, 1_000, 10_000, 100_000, 1_000_000)