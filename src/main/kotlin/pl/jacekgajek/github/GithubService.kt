package pl.jacekgajek.github

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class GithubService(private val client: GithubClient) {
    suspend fun getRepositories(userName: String): Flow<RepositoryDto> {
        val owner = client.getUserInfo(userName)
        val repos = client.getRepositories(owner).asFlow()
            .filterNot { it.fork }
            .map { repo ->
                val branches = client.getBranches(repo).map { BranchDto(it.name, it.commit.sha) }
                RepositoryDto(
                    owner = repo.owner.login,
                    name = repo.name,
                    branches = branches
                )
            }
        return repos
    }

    data class RepositoryDto(val owner: String, val name: String, val branches: List<BranchDto>)
    data class BranchDto(val name: String, val lastCommitSha: String?)
}

