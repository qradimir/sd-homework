package ru.itmo.ctddev.sorokin.sd.actors

import akka.actor.Props
import org.junit.Test
import scala.concurrent.duration.Duration

import java.util.concurrent.TimeUnit

class DDGActorTest : AbstractActorTest() {

    @Test
    fun testSearch() = test {
        val actorRef = system.actorOf(Props.create(SearchEngineActor::class.java, SearchEngine.DuckDuckGo.requestHandler), "ddg")
        actorRef.tell(WebRequest("test"), testActor())
        expectMsgClass(Duration.apply(30, TimeUnit.SECONDS), WebResponse::class.java)
    }

}