package ru.itmo.ctddev.sorokin.sd.react

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
open class CatalogAppTests {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    @Test
    fun testCreateUser() {
        val user = defaultUser()

        userRepository.deleteAll().block()

        webTestClient.post().uri("/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .body(Mono.just(user), UserInfo::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.cur").isEqualTo("usd")

        val createdUser = userRepository.findAll().next().block()
        assertEquals(user.id, createdUser?.id)
        assertEquals(user.cur, createdUser?.cur)
    }

    @Test
    fun testCreateProduct() {
        val user = defaultUser()
        userRepository.save(user).block()

        val product = defaultProduct()

        webTestClient.post().uri("/product?id=$USER_NAME")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .body(Mono.just(product), Product::class.java)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath(".name").isNotEmpty
                .jsonPath("$.price").isNumber

        val createdProduct = productRepository.findAll().next().block()
        assertEquals(product.name, createdProduct?.name)
        assertEquals(product.price!! * Currency.USD.coef, createdProduct?.price)
    }

    @Test
    fun testGetProducts() {
        val user = defaultUser()
        userRepository.save(user).block()

        val product = defaultProduct()

        productRepository.save(product)

        webTestClient.get().uri("/products?id=$USER_NAME")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Product::class.java)
    }

    private fun defaultUser() = UserInfo().apply {
        id = USER_NAME
        currency = Currency.USD
    }

    private fun defaultProduct(): Product {
        return Product().apply {
            name = PRODUCT_NAME
            price = 1.0
        }
    }

    val USER_NAME = "testUser"
    val PRODUCT_NAME = "test"
}