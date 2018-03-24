package ru.itmo.ctddev.sorokin.sd.actors

import com.google.gson.JsonParser
import java.util.ArrayList
import org.jsoup.nodes.Document
import java.net.URLDecoder

class SearchRequest(val request: String)

class WebRequest(val request: String)

class WebResponse(val searchEngine: SearchEngine, val results: List<String>) {

    companion object {

        fun truncated(searchEngine: SearchEngine, results: List<String>, maxSize : Int = 5)
                = WebResponse(searchEngine, if (results.size > 5) results.subList(0, 5) else results)

        fun fromBing(jsonAsStr: String): WebResponse {
            val json = JsonParser().parse(jsonAsStr).asJsonObject

            val results = ArrayList<String>()
            val array = json.get("webPages").asJsonObject.get("value").asJsonArray
            for (elem in array) {
                results.add(elem.asJsonObject.get("displayUrl").asString)
            }

            return truncated(SearchEngine.Bing, results)
        }

        fun fromDDG(document: Document): WebResponse {
            val results = ArrayList<String>()
            val elemResults = document.getElementById("links").getElementsByClass("results_links")

            for (result in elemResults) {
                val title = result.getElementsByClass("links_main").first().getElementsByTag("a").first()
                results.add(title.text() + ", " + title.attr("href"))
            }
            return truncated(SearchEngine.DuckDuckGo, results)
        }

        fun fromGoogle(doc: Document): WebResponse {
            val results = ArrayList<String>()

            val links = doc.select(".g>div>.rc>.r>*")
            for (link in links) {
                val title = link.text()
                val url = link.absUrl("href").let { URLDecoder.decode(it, "UTF-8") }

                if (url.startsWith("http")) {
                    results.add("$title, $url")
                }
            }
            return truncated(SearchEngine.Google, results)
        }

    }
}
