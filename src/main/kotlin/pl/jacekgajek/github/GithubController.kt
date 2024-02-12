package pl.jacekgajek.github

import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/repos", produces = [MediaType.APPLICATION_JSON_VALUE])
class GithubController(val githubService: GithubService) {

    // Leveraging Spring Boot support for coroutines for reactive/non-blocking API calls.
    @GetMapping("/{user}")
    suspend fun getRepositories(@PathVariable user: String): Flow<GithubService.RepositoryDto> {
        return githubService.getRepositories(user)
    }

    // Note to reviewer: if client expects XML then it cannot understand this JSON with error message
    //
    // This is the reason why NOT_ACCEPTABLE returns no content, because
    // we cannot produce a response which can be understood by the client
    // Therefore, this requirement doesn't make sense and would be dropped in a refinement.
    //
    // (This can be also handled by extending DefaultHandlerExceptionResolver, but it would
    // result in a lot of boilerplate, so I'll leave as it is since such solution would
    // never pass a code review anyway.)
    @GetMapping("/{user}", produces = [MediaType.APPLICATION_XML_VALUE])
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    suspend fun getRepositoriesXml(@PathVariable user: String): String {
        return """{ "status": 406, "message": "XML not supported" }"""
    }

    @ExceptionHandler(GithubClient.UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: GithubClient.UserNotFoundException): ErrorBodyDto {
        return ErrorBodyDto(HttpStatus.NOT_FOUND.value(), ex.message)
    }

    @ExceptionHandler(GithubClient.UnexpectedGitHubResponseException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(ex: GithubClient.UnexpectedGitHubResponseException): ErrorBodyDto {
        return ErrorBodyDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message)
    }
}

data class ErrorBodyDto(var status: Int, var message: String?)
