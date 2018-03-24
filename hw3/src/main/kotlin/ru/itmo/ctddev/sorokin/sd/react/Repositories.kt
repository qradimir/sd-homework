package ru.itmo.ctddev.sorokin.sd.react

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ProductRepository : ReactiveMongoRepository<Product, String>
interface UserRepository : ReactiveMongoRepository<UserInfo, String>