package pl.jacekgajek.github

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.with

@TestConfiguration(proxyBeanMethods = false)
class TestGithubApplication

fun main(args: Array<String>) {
    fromApplication<GithubApplication>().with(TestGithubApplication::class).run(*args)
}
