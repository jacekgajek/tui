package pl.jacekgajek.github

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    fun getGithubWebClient(): WebClient =
        WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader( "X-GitHub-Api-Version", "2022-11-28")
            .build()
}