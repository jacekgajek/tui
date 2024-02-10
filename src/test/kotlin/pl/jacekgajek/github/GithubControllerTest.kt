package pl.jacekgajek.github

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.reset
import org.mockito.kotlin.wheneverBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

private const val USER_NAME = "jacekgajek"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT1H")
class GithubControllerTest {

    @Autowired
    private lateinit var githubClient: GithubClient

    @Autowired
    private lateinit var web: WebTestClient

    @BeforeEach
    fun setup() {
        web = web.mutate()
            .responseTimeout(Duration.ofDays(1))
            .build()
        reset(githubClient)
    }

    @Test
    fun returns406ifXmlIsRequested() {
        wheneverBlocking { githubClient.getUserInfo(any()) }.doThrow(GithubClient.UserNotFoundException(USER_NAME))
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
        wheneverBlocking { githubClient.getUserInfo(any()) }.doThrow(GithubClient.UserNotFoundException(USER_NAME))
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
        val userDto = GithubClient.UserInfoDto("/repos")
        wheneverBlocking { githubClient.getUserInfo(USER_NAME) }.thenReturn(userDto)
        val repo1 = GithubClient.RepoDto("repo2", "/branches", true, GithubClient.RepoDto.RepoOwner("somebody"))
        val repo2 = GithubClient.RepoDto("repo1", "/branches", false, GithubClient.RepoDto.RepoOwner(USER_NAME))
        val reposDto = listOf(repo1, repo2)
        wheneverBlocking { githubClient.getRepositories(userDto) }.thenReturn(reposDto)
        val branches = listOf(
            GithubClient.BranchDto("main", GithubClient.BranchDto.CommitDto("123")),
            GithubClient.BranchDto("feature", GithubClient.BranchDto.CommitDto("234")),
        )
        wheneverBlocking { githubClient.getBranches(repo2) }.thenReturn(branches)

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
}