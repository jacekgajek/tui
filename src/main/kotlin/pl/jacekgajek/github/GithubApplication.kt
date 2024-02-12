package pl.jacekgajek.github

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
// Required by Graal
@RegisterReflectionForBinding(
    GithubService.RepositoryDto::class, GithubClient.BranchDto::class,
    GithubService.BranchDto::class,
    GithubClient.CommitDto::class,
    GithubClient.RepoDto::class,
    GithubClient.RepoOwner::class,
    GithubClient.UserInfoDto::class,
)
class GithubApplication

fun main(args: Array<String>) {
    runApplication<GithubApplication>(*args)
}
