package pl.jacekgajek.github

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import org.springframework.web.reactive.function.client.awaitBody
import reactor.core.publisher.Mono

@Service
class GithubClient(private val client: WebClient) {
    suspend fun getUserInfo(user: String): UserInfoDto =
        client.get()
            .uri("/users/{username}", user)
            .retrieve()
            .onStatus({ it == HttpStatus.NOT_FOUND }) { Mono.error(UserNotFoundException(user)) }
            .handleUnexpectedGithubResponse()
            .awaitBody()

    suspend fun getRepositories(owner: UserInfoDto): List<RepoDto> =
        client.get()
            .uri(owner.reposUrl.stripSuffixInBrackets)
            .retrieveAndAwait()

    suspend fun getBranches(repo: RepoDto): List<BranchDto> =
        client.get()
            .uri(repo.branchesUrl.stripSuffixInBrackets)
            .retrieveAndAwait()

    data class BranchDto(val name: String, val commit: CommitDto)
    data class CommitDto(val sha: String)
    data class RepoDto(
        val name: String,
        @JsonProperty("branches_url") val branchesUrl: String,
        val fork: Boolean,
        val owner: RepoOwner
    )
    data class RepoOwner(val login: String)
    data class UserInfoDto(
        @JsonProperty("repos_url") val reposUrl: String
    )

    class UserNotFoundException(user: String) : RuntimeException("User $user not found")
    class UnexpectedGitHubResponseException(status: HttpStatusCode) :
        RuntimeException("Github responded with ${status.value()} status.")

    private suspend inline fun <reified T> RequestHeadersSpec<*>.retrieveAndAwait(): T {
        return this
            .retrieve()
            .handleUnexpectedGithubResponse()
            .awaitBody()
    }

    fun ResponseSpec.handleUnexpectedGithubResponse(): ResponseSpec {
        return this.onStatus({ it.isError }) { Mono.error(UnexpectedGitHubResponseException(it.statusCode())) }
    }

    /**
     * Returns a new string by removing the substring enclosed in curly brackets from the end of this string.
     *
     * @return A new string with the substring enclosed in curly brackets removed from the end.
     */
    private val String.stripSuffixInBrackets: String
        get() = this.takeIf { endsWith("}") }
            ?.let { lastIndexOf('{') }
            ?.takeIf { it >= 0 }
            ?.let { substring(0, it) }
            ?: this
}
