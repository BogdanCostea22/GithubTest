package ro.githubdemo.demo.controllers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

@JsonAutoDetect(
    fieldVisibility = Visibility.NONE,
    setterVisibility = Visibility.NONE,
    getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE,
    creatorVisibility = Visibility.NONE
)
sealed class CustomAppException(
    @get:JsonProperty("message")
    val messageResponse: String,
    val status: HttpStatus
) : RuntimeException(messageResponse) {

    @get:JsonProperty("status")
    val statusValue = status.value()

    class InvalidHeader :
        CustomAppException(messageResponse = "Invalid header set in the request!", status = HttpStatus.NOT_ACCEPTABLE)

    class UserNotFound :
        CustomAppException(messageResponse = "User not exist!", status = HttpStatus.NOT_FOUND)

    class GithubConnectionProblem :
        CustomAppException(
            messageResponse = "Failed to establish a connection with Github!",
            status = HttpStatus.INTERNAL_SERVER_ERROR
        )

    class FailedToFetchRepositoryBranches :
        CustomAppException(
            messageResponse = "Failed to fetch the repository branches!",
            status = HttpStatus.INTERNAL_SERVER_ERROR
        )

    class ConnectionProblem :
        CustomAppException(messageResponse = "Encountered connection problems!", status = HttpStatus.BAD_REQUEST)

    class EndpointServerError :
        CustomAppException(
            messageResponse = "Failed to establish a connection with the remote remote server!",
            status = HttpStatus.SERVICE_UNAVAILABLE
        )

    class NotFoundException :
        CustomAppException(messageResponse = "Can't find the resource!", status = HttpStatus.NOT_FOUND)

    class InternalServerError :
        CustomAppException(messageResponse = "Internal server error!", status = HttpStatus.INTERNAL_SERVER_ERROR)
}
