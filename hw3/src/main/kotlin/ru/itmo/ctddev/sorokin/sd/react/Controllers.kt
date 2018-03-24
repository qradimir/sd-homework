package ru.itmo.ctddev.sorokin.sd.react

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

import javax.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice


@RestController
class ProductController {
    @Autowired
    lateinit var productRepository: ProductRepository
    @Autowired
    lateinit var userRepository: UserRepository

    @GetMapping("/products")
    fun getAllProducts(@RequestParam id: String): Flux<Product> {
        val products = productRepository.findAll()
        return userRepository
                .findById(id)
                .flatMapMany { user ->
                    products.map { convertFromRuble(user, it) }
                }
    }

    @PostMapping("/product")
    fun createProduct(@Valid @RequestBody product: Product, @RequestParam id: String) =
            productRepository
                    .existsById(product.name!!)
                    .flatMap {
                        assertAlreadyExist(it)
                        val productMono = userRepository.findById(id).map { convertToRuble(it, product) }
                        productRepository.saveAll(productMono).next()
                    }

    private fun convertToRuble(user: UserInfo, product: Product) =
            user.currency?.let { product.convertPriceToRuble(it) } ?: product

    private fun convertFromRuble(user: UserInfo, product: Product) =
            user.currency?.let { product.convertPriceFromRuble(it) } ?: product
}

@RestController
class RegisterController {
    @Autowired
    lateinit var userRepository: UserRepository

    @PostMapping("/user")
    fun createUser(@Valid @RequestBody user: UserInfo) =
            userRepository
                    .existsById(user.id!!)
                    .flatMap {
                        assertAlreadyExist(it)
                        userRepository.save(user)
                    }

    @GetMapping("/users")
    fun getAllUsers() =
            userRepository.findAll()
}

@ControllerAdvice
class RestResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [AlreadyExistsException::class])
    fun handleConflict(ex: AlreadyExistsException): ResponseEntity<Any> =
            ResponseEntity.badRequest().build()
}

class AlreadyExistsException(reason: String? = null) : IllegalArgumentException(reason)

fun assertAlreadyExist(exists: Boolean?) {
    if (exists == true) {
        throw AlreadyExistsException("Already exist")
    }
}
