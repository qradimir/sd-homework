package ru.itmo.ctddev.sorokin.sd.actors

import akka.actor.AbstractActor
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


typealias RequestHandler = SearchEngineActor.(WebRequest) -> Unit

class SearchEngine(
        val name: String,
        val requestHandler: RequestHandler
) {

    companion object {

        val Google = SearchEngine("Google") {
            val doc = Jsoup.connect(it.url(GOOGLE_URL)).get()
            response(WebResponse.fromGoogle(doc))
        }

        val DuckDuckGo = SearchEngine("DuckDuckGo") {
            val doc = Jsoup.connect(it.url(DDG_URL)).get()
            response(WebResponse.fromDDG(doc))
        }

        val Bing = SearchEngine("Bing") {
            val url = URL(it.url(BING_URL))
            val connection = url.openConnection() as HttpsURLConnection
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", System.getProperty("AzureKey"))

            val json = Scanner(connection.inputStream).useDelimiter("\\A").next()

            response(WebResponse.fromBing(json))
        }

        val all = listOf(Google, Bing, DuckDuckGo)
    }
}

class SearchEngineActor(val requestHandler: RequestHandler) : AbstractActor() {
    override fun createReceive(): Receive {
        return receiveBuilder()
                .match(WebRequest::class.java) { requestHandler(it) }
                .matchAny { unhandled(it)}
                .build()
    }

    fun response(response: WebResponse) {
        sender.tell(response, self)
        context().stop(self)
    }

    fun WebRequest.url(host: String) = host + URLEncoder.encode(request, CHARSET)
}

private const val GOOGLE_URL = "https://www.google.com/search?q="
private const val DDG_URL = "https://duckduckgo.com/html/?q="
private const val BING_URL = "https://api.cognitive.microsoft.com/bing/v7.0/search?q="
private const val CHARSET = "UTF-8"

