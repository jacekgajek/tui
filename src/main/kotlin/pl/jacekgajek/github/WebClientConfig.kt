package pl.jacekgajek.github

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class WebClientConfig {

    @Bean
    fun getGithubWebClient(): WebClient =
        WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader( "X-GitHub-Api-Version", "2022-11-28")
            .build()
}