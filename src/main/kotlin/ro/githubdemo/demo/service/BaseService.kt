package ro.githubdemo.demo.service

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import ro.githubdemo.demo.controllers.CustomAppException

open class BaseService {
    private fun basicMapper(webClientResponseException: WebClientResponseException): CustomAppException =
        when (webClientResponseException.statusCode) {
            HttpStatus.NOT_FOUND -> CustomAppException.NotFoundException()
            else -> CustomAppException.ConnectionProblem()
        }

    suspend fun <R> handleRequest(
        errorMapper: (WebClientResponseException) -> CustomAppException = ::basicMapper,
        request: suspend () -> R
    ): R =
        try {
            request.invoke()
        } catch (exc: Exception) {
            println(exc.message)
            when (exc) {
                is WebClientResponseException -> {
                    throw errorMapper(exc)
                }

                else -> throw CustomAppException.EndpointServerError()
            }
        }
}
