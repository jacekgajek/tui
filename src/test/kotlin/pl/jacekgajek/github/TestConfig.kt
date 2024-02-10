package pl.jacekgajek.github

import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Configuration


@Configuration
class TestConfig {
    @MockBean
    lateinit var githubClient: GithubClient
}