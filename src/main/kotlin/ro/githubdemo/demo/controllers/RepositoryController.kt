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
import kotlinx.coroutines.flow.collectLatest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ro.githubdemo.demo.usecases.RepositoryWithBranches
import ro.githubdemo.demo.usecases.UseCaseStructure

@RestController
class RepositoryController(
    private val getRepositoryUseCase: UseCaseStructure<String, RepositoryWithBranches>
) {

    @Operation(
        summary = "Find all repositories for a given user", parameters = [
            Parameter(
                `in` = ParameterIn.HEADER,
                description = "The request needs to have an header.",
                name = "Accept",
                content = [Content(
                    schema = Schema(
                        type = "string",
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
    @GetMapping("/repositories")
    suspend fun getData(
        @RequestHeader headers: HttpHeaders?,
        @RequestParam("username") username: String
    ): Flow<RepositoryWithBranches> {
        return getRepositoryUseCase.execute(username, headers)
    }
}
