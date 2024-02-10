package pl.jacekgajek.github

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GithubApplication

fun main(args: Array<String>) {
    runApplication<GithubApplication>(*args)
}
