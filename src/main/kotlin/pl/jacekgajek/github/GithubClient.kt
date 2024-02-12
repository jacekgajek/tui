package pl.jacekgajek.github

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import pl.jacekgajek.github.GitHubApiUtils.handleUnexpectedGithubResponse
import pl.jacekgajek.github.GitHubApiUtils.retrieveAndAwait
import pl.jacekgajek.github.GitHubApiUtils.stripSuffixInBrackets
import reactor.core.publisher.Mono

/**
 * Note about chosen implementation:
 *
 * I decided to use the URLs which are returned by github in the /users/ request, so this
 * client must be dynamic.
 *
 * Another solution would be to use a Spring's declarative client, but that'd
 * require hard-coding URLs. It's OK since the github API is versioned. I'd probably
 * bring this up on a daily for a short discussion which way we should go.
 */
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
}
