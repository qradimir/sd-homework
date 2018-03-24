package ru.itmo.ctddev.sorokin.sd.aop.demo

fun main(args: Array<String>) {
    sleepForASecond()
    for (i in 1..15) {
        doStuff(i)
        doOtherStuff(i)
    }
    println()
    println("Done!")
}

fun doStuff(i: Int) {
    for (j in 1..i) {
        nestedStuff(j)
    }
}

fun doOtherStuff(i: Int) {
    for (j in i..30) {
        nestedStuff(j)
    }
}

fun nestedStuff(j: Int) {
    for (t in 1..j) {
        printInt(t)
        teek()
    }
}

fun printInt(t: Int) = print(t)

fun teek() = Thread.sleep(1)

fun sleepForASecond() {
    Thread.sleep(1000)
    println("Hi")
}