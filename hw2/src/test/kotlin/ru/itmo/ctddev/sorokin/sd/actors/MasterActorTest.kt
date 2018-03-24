package ru.itmo.ctddev.sorokin.sd.actors

import akka.actor.ActorRef
import akka.actor.Props
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.junit.*
import scala.concurrent.duration.Duration

import java.util.concurrent.TimeUnit

val emptyShutdown = Runnable {}

class MasterSearchActorTest : AbstractActorTest() {

    fun createMaster(vararg answerList: Boolean) =
            system.actorOf(Props.create(MasterSearchActor::class.java, emptyShutdown, answerList.mapIndexed { i, b -> mockEngine(i, b) }), "master")

    fun mockEngine(i: Int, answer: Boolean): SearchEngine {
        val engineRef = Array<SearchEngine?>(1) { null }
        val engine = SearchEngine("mock-delay$i") {
            if (answer) {
                response(WebResponse(engineRef.get(0)!!, arrayListOf("done")))
            }
        }
        engineRef[0] = engine
        return engine
    }

    @Test
    fun testMaster1() = test {
        val actorRef = createMaster(true, true, true)
        actorRef.tell(SearchRequest("test"), testActor())
        expectTermination(actorRef, 5)
    }

    @Test
    fun testMaster2() = test {
        val actorRef = createMaster(false)
        actorRef.tell(SearchRequest("test"), testActor())
        expectTermination(actorRef, 35)
    }

    private fun expectTermination(actorRef: ActorRef?, secs: Long) {
        val testProbe = TestProbe(system)
        testProbe.watch(actorRef)
        testProbe.expectTerminated(actorRef, Duration.apply(secs, TimeUnit.SECONDS))
    }

}