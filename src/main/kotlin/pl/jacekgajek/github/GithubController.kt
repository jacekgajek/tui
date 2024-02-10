package pl.jacekgajek.github

import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/repos")
class GithubController(val githubService: GithubService) {
    @GetMapping("/{user}")
    suspend fun getRepositories(@PathVariable user: String): Flow<GithubService.RepositoryDto> {
        return githubService.getRepositories(user)
    }

    @ExceptionHandler(GithubClient.UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: GithubClient.UserNotFoundException): ErrorBodyDto {
        return ErrorBodyDto(HttpStatus.NOT_FOUND.value(), ex.message)
    }
}

data class ErrorBodyDto(val status: Int, val message: String?)
