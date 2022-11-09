package ro.githubdemo.demo.controllers

import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ro.githubdemo.demo.usecases.GetRepositoryUseCase
import ro.githubdemo.demo.usecases.RepositoryWithBranches

@RestController
class RepositoryController(
    private val getRepositoryUseCase: GetRepositoryUseCase
) {

    @GetMapping("/hello")
    suspend fun getData(
        @RequestHeader header: Map<String, String>,
        @RequestParam("username") username: String
    ): Flow<RepositoryWithBranches> {
        return getRepositoryUseCase.execute(header, username)
    }
}

sealed class CustomAppException(
    messageResponse: String,
    val code: HttpStatus
) : RuntimeException(messageResponse) {

    class InvalidHeader :
        CustomAppException(messageResponse = "Invalid header set in the request!", code = HttpStatus.NOT_ACCEPTABLE)

    class UserNotFound :
        CustomAppException(messageResponse = "User not exist!", code = HttpStatus.NOT_FOUND)

    class GithubConnectionProblem :
        CustomAppException(messageResponse = "Failed to connect to Github!", code = HttpStatus.BAD_REQUEST)

    class ConnectionProblem :
        CustomAppException(messageResponse = "Encountered connection problems!", code = HttpStatus.BAD_REQUEST)

    class EndpointServerError :
        CustomAppException(
            messageResponse = "Failed to establish a connection with the remote remote server!",
            code = HttpStatus.SERVICE_UNAVAILABLE
        )

    class NotfoundException :
        CustomAppException(messageResponse = "Can't find the resource!", code = HttpStatus.NOT_FOUND)

    override fun toString(): String =
        "{\"status\": ${code.value()} \"Messsage\": $message}"
}

@RestControllerAdvice
class ErrorHandler {

    @ExceptionHandler(CustomAppException::class)
    suspend fun handleExceptions(exc: CustomAppException): List<String> {
        val message = exc.toString()
        return List(exc.code.value()) { message }
    }

}
