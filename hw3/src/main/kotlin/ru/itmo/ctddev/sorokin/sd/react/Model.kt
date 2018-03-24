package ru.itmo.ctddev.sorokin.sd.react

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
class Product {
    @Id
    var name: String? = null
    var price: Double? = null
}


@Document(collection = "users")
class UserInfo {
    @Id
    var id: String? = null
    var cur: String? = null
}

var UserInfo.currency: Currency?
    get() = Currency.all[cur]
    set(value) {
        cur = value?.name
    }