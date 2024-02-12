package pl.jacekgajek.github

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

object GitHubApiUtils {
    suspend inline fun <reified T> WebClient.RequestHeadersSpec<*>.retrieveAndAwait(): T {
        return this
            .retrieve()
            .handleUnexpectedGithubResponse()
            .awaitBody()
    }

    fun WebClient.ResponseSpec.handleUnexpectedGithubResponse(): WebClient.ResponseSpec {
        return this.onStatus({ it.isError }) { Mono.error(GithubClient.UnexpectedGitHubResponseException(it.statusCode())) }
    }

    /**
     * GitHub returns URLs in form 'https://api.github.com/.../branches{/branch}'. This methods
     * strips the part in curly braces.
     */
    val String.stripSuffixInBrackets: String
        get() = this.takeIf { endsWith("}") }
            ?.let { lastIndexOf('{') }
            ?.takeIf { it >= 0 }
            ?.let { substring(0, it) }
            ?: this
}