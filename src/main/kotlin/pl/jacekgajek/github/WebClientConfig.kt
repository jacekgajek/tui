package pl.jacekgajek.github

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(@Value("\${jacekgajek.github-api-url}") private val githubApi: String) {
    @Bean
    fun getGithubWebClient(): WebClient =
        WebClient.builder()
            .baseUrl(githubApi)
            .defaultHeader( "X-GitHub-Api-Version", "2022-11-28")
            .build()
}