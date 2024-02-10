package pl.jacekgajek.github

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.wheneverBlocking

private const val USER_NAME = "jacekgajek"

class GithubServiceTest {
    @Test
    fun forksAreNotReturned() {
        // GIVEN
        val githubClient = mock<GithubClient>()

        val userDto = GithubClient.UserInfoDto("/repos")
        wheneverBlocking { githubClient.getUserInfo(USER_NAME) }.thenReturn(userDto)
        val repo1 = GithubClient.RepoDto("repo2", "/branches", true, GithubClient.RepoDto.RepoOwner("somebody"))
        val repo2 = GithubClient.RepoDto("repo1", "/branches", false, GithubClient.RepoDto.RepoOwner(USER_NAME))
        val reposDto = listOf(repo1, repo2)
        wheneverBlocking { githubClient.getRepositories(userDto) }.thenReturn(reposDto)
        val branchesInRepo = listOf(
            GithubClient.BranchDto("main", GithubClient.BranchDto.CommitDto("123")),
            GithubClient.BranchDto("feature", GithubClient.BranchDto.CommitDto("234")),
        )
        wheneverBlocking { githubClient.getBranches(repo2) }.thenReturn(branchesInRepo )

        // WHEN
        val service = GithubService(githubClient)
        val result: List<GithubService.RepositoryDto> = runBlocking { service.getRepositories(USER_NAME).toList() }

        // THEN
        assertThat(result).hasSize(1)
        with(result[0]) {
            assertThat(branches).hasSize(2)
            assertThat(owner).isEqualTo(USER_NAME)
            assertThat(name).isEqualTo("repo1")
            assertThat(branches[0].name).isEqualTo("main")
            assertThat(branches[0].lastCommitSha).isEqualTo("123")
            assertThat(branches[1].name).isEqualTo("feature")
            assertThat(branches[1].lastCommitSha).isEqualTo("234")
        }

    }

    @Test
    fun throwsIfUserNotFound() {
        // GIVEN
        val githubClient = mock<GithubClient>()
        wheneverBlocking { githubClient.getUserInfo(any()) }.doThrow(GithubClient.UserNotFoundException(USER_NAME))

        // WHEN
        val service = GithubService(githubClient)
       assertThatThrownBy { runBlocking { service.getRepositories(USER_NAME) } }
           .isInstanceOfAny(GithubClient.UserNotFoundException::class.java)
    }
}