package ru.itmo.ctddev.sorokin.sd.react

class Currency(val name: String, val coef: Double) {


    companion object {
        val RUB = Currency("rub", 1.0)
        val USD = Currency("usd", 57.24)
        val EUR = Currency("eur", 70.73)

        val all = currencies(RUB, USD, EUR)

        private fun currencies(vararg cs: Currency) = HashMap<String, Currency>().apply {
            for (c in cs) {
                this[c.name] = c
            }
        }
    }
}

fun Product.convertPriceFromRuble(c: Currency): Product {
    val pr = price ?: return this
    return Product().also {
        it.name = name
        it.price = pr / c.coef
    }
}

fun Product.convertPriceToRuble(c: Currency): Product {
    val pr = price ?: return this
    return Product().also {
        it.name = name
        it.price = pr * c.coef
    }
}