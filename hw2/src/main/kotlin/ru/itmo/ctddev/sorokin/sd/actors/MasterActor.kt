package ru.itmo.ctddev.sorokin.sd.actors

import java.util.concurrent.TimeUnit
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.AbstractActor
import akka.actor.ReceiveTimeout
import scala.concurrent.duration.Duration
import java.util.*
import akka.actor.ActorSystem


class MasterSearchActor(
        val shutdown: Runnable,
        val engines: List<SearchEngine> = SearchEngine.all
) : AbstractActor() {
    private val results = HashMap<SearchEngine, List<String>>()

    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(SearchRequest::class.java) { processStartRequest(it) }
                .match(WebResponse::class.java) { processWebResponse(it) }
                .match(ReceiveTimeout::class.java) { processReceiveTimeout(it) }
                .matchAny { unhandled(it) }
                .build()
    }

    private fun processReceiveTimeout(timeout: ReceiveTimeout) {
        finishSearch()
    }

    private fun processWebResponse(webResponse: WebResponse) {
        results.put(webResponse.searchEngine, webResponse.results)

        if (results.size == engines.size) {
            finishSearch()
        }
    }

    private fun finishSearch() {
        for ((engine, results) in results.entries) {
            for (result in results) {
                println("${engine.name} ---> $result")
            }
        }
        context().stop(self)
    }

    private fun processStartRequest(searchRequest: SearchRequest) {
        val req = searchRequest.request

        engines.forEach { it.start(req) }

        context().setReceiveTimeout(Duration.apply(30L, TimeUnit.SECONDS))
    }

    private fun SearchEngine.start(request: String) {
        val searchChild = context().actorOf(Props.create(SearchEngineActor::class.java, requestHandler), name)
        searchChild.tell(WebRequest(request), self)
    }

    override fun postStop() {
        super.postStop()
        shutdown.run()
    }

}

fun main(args: Array<String>) {
    val search = args.getOrNull(0) ?: "software design"
    println("Searching for '${search}'")

    val actors = ActorSystem.create("main")
    val shutdown = Runnable { actors.terminate() }
    val master = actors.actorOf(Props.create(MasterSearchActor::class.java, actors), "master")

    master.tell(SearchRequest(search), ActorRef.noSender())
}