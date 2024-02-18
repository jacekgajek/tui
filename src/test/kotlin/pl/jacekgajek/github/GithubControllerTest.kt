package pl.jacekgajek.github

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType


private const val USER_NAME = "jacekgajek"

class GithubControllerTest : AbstractIntegrationTest() {

    @Test
    fun returns406ifXmlIsRequested() {
        web.get()
            .uri("/api/v1/repos/{username}", USER_NAME)
            .header(HttpHeaders.ACCEPT, "application/xml")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE.value())
            .expectBody()
            .json(
                """
                {
                  "status": 406,
                  "message": "XML not supported"
                }
            """.trimIndent()
            )
    }

    @Test
    fun returns404ifUserNotFound() {
        server.stubFor(
            WireMock.get(WireMock.urlMatching("http://localhost:8088/users/$USER_NAME"))
                .willReturn(WireMock.aResponse().withStatus(404))
        )

        web.get()
            .uri("/api/v1/repos/{username}", USER_NAME)
            .header(HttpHeaders.ACCEPT, "application/json")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .json(
                """
                {
                  "status": 404,
                  "message": "User $USER_NAME not found"
                }
            """.trimIndent()
            )
    }

    @Test
    fun forksAreNotReturned() {
        // GIVEN
        val userDto = GithubClient.UserInfoDto("/users/jacekgajek/repos")
        mockCall("/users/$USER_NAME", userDto)

        val repo1 = GithubClient.RepoDto("repo2", "/repos/$USER_NAME/repo2/branches", true, GithubClient.RepoOwner("somebody"))
        val repo2 = GithubClient.RepoDto("repo1", "/repos/$USER_NAME/repo1/branches", false, GithubClient.RepoOwner(USER_NAME))
        val reposDto = listOf(repo1, repo2)
        mockCall("/users/$USER_NAME/repos", reposDto)

        val branches = listOf(
            GithubClient.BranchDto("main", GithubClient.CommitDto("123")),
            GithubClient.BranchDto("feature", GithubClient.CommitDto("234")),
        )
        mockCall("/repos/$USER_NAME/repo1/branches", branches)

        // THEN
        web.get()
            .uri("/api/v1/repos/{username}", USER_NAME)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json(
                """
                [
                  {
                    "name": "repo1",
                    "owner": "$USER_NAME",
                    "branches": [
                      {
                        "name": "main",
                        "lastCommitSha": "123"
                      },
                      {
                        "name": "feature",
                        "lastCommitSha": "234"
                      }
                    ]
                  }
                ]
            """.trimIndent()
            )
    }

    private fun <T> mockCall(url: String, branches: T) {
        server.stubFor(
            WireMock.get(WireMock.urlEqualTo(url))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(branches))
                )
        )
    }
}
