package ro.githubdemo.demo.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlinx.coroutines.flow.Flow
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ro.githubdemo.demo.usecases.GetRepositoryUseCase
import ro.githubdemo.demo.usecases.RepositoryWithBranches

@RestController
class RepositoryController(
    private val getRepositoryUseCase: GetRepositoryUseCase
) {

    @Operation(
        summary = "Find all repositories for a given user", parameters = [
            Parameter(
                `in` = ParameterIn.HEADER,
                description = "Custom Header To be Pass",
                name = "Content-Type",
                content = [Content(
                    schema = Schema(
                        type = HttpHeaders.CONTENT_TYPE,
                        allowableValues = [MediaType.APPLICATION_JSON_VALUE]
                    )
                )]
            )
        ]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Found repositories", content = [
                    (Content(
                        mediaType = "application/json", array = (
                                ArraySchema(schema = Schema(implementation = RepositoryWithBranches::class)))
                    ))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found the username in the system",
                content = [Content()]
            ),
            ApiResponse(responseCode = "406", description = "Invalid header format!", content = [Content()]),
        ]
    )
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
    suspend fun handleExceptions(exc: CustomAppException): CustomAppException {
        return exc
    }

}
