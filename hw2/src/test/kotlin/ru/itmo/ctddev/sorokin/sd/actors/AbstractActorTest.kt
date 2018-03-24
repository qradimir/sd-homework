package ru.itmo.ctddev.sorokin.sd.actors

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.junit.After
import org.junit.Before
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

open class AbstractActorTest() {

    lateinit var system: ActorSystem

    @Before
    fun init() {
        system = ActorSystem.create()
    }

    @After
    fun dispose() {
        TestKit.shutdownActorSystem(system, Duration.apply(70, TimeUnit.SECONDS), true)
    }

    fun test(body: TestKit.() -> Unit) {
        object : TestKit(system) {
            init {
                body()
            }
        }
    }
}