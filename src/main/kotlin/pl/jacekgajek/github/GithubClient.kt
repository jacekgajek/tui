package pl.jacekgajek.github

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import reactor.core.publisher.Mono

@Service
class GithubClient(private val client: WebClient) {
    suspend fun getUserInfo(user: String): UserInfoDto = client.get()
        .uri("/users/{username}", user)
        .retrieve()
        .onStatus({ it == HttpStatus.NOT_FOUND }) { Mono.error(UserNotFoundException(user)) }
        .handleUnexpectedGithubResponse()
        .awaitBodyOrNull() ?: throw UserNotFoundException(user)

    suspend fun getRepositories(owner: UserInfoDto): List<RepoDto> =
        client.get()
            .uri(owner.reposUrl.stripSuffixInBrackets)
            .retrieve()
            .handleUnexpectedGithubResponse()
            .awaitBody()

    suspend fun getBranches(repo: RepoDto): List<BranchDto> =
        client.get()
            .uri(repo.branchesUrl.stripSuffixInBrackets)
            .retrieve()
            .handleUnexpectedGithubResponse()
            .awaitBody()

    private val String.stripSuffixInBrackets: String
        get() = this.takeIf { endsWith("}") }
            ?.let { lastIndexOf('{') }
            ?.takeIf { it >= 0 }
            ?.let { substring(0, it) }
            ?: this

    fun ResponseSpec.handleUnexpectedGithubResponse(): ResponseSpec {
        return this.onStatus({ it.isError }) { Mono.error(UnexpectedGitHubResponseException(it.statusCode())) }
    }

    data class BranchDto(val name: String, val commit: CommitDto) {
        data class CommitDto(val sha: String)
    }

    data class RepoDto(
        val id: Long,
        @JsonProperty("branches_url")
        val branchesUrl: String,
        val fork: Boolean,
        val owner: RepoOwner
    ) {
        data class RepoOwner(val login: String)
    }

    data class UserInfoDto(
        val id: Long,
        @JsonProperty("repos_url")
        val reposUrl: String
    )

    class UserNotFoundException(user: String) : Exception("User $user not found")
    class UnexpectedGitHubResponseException(status: HttpStatusCode) :
        Exception("Github responded with ${status.value()} status.")
}
