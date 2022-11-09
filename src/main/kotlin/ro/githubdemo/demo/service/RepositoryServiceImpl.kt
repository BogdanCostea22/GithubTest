package ro.githubdemo.demo.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import ro.githubdemo.demo.config.AppProperties
import ro.githubdemo.demo.controllers.CustomAppException
import ro.githubdemo.demo.service.model.RepositoryBranchResponse
import ro.githubdemo.demo.service.model.RepositoryResponse

@Repository
class RepositoryServiceImpl(
    private val webClient: WebClient,
    private val properties: AppProperties
) : RepositoryService, BaseService() {
    private fun githubErrorMapper(webClientResponseException: WebClientResponseException): CustomAppException =
        when (webClientResponseException.statusCode) {
            HttpStatus.NOT_FOUND -> CustomAppException.UserNotFound()
            else -> CustomAppException.GithubConnectionProblem()
        }

    override suspend fun getAllRepositories(username: String): Flow<RepositoryData> =
        handleRequest(::githubErrorMapper) {
            webClient.get()
                .uri("${properties.githubUrl}/users/${username}/repos")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<List<RepositoryResponse>>()
                .asFlow()
                .map(RepositoryResponse.Companion::to)
        }


    override suspend fun getBranchesFor(username: String, repositoryName: String): List<RepositoryDetails> =
        handleRequest {
            webClient.get()
                .uri("${properties.githubUrl}/repos/${username}/${repositoryName}/branches")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .awaitBody<List<RepositoryBranchResponse>>()
                .map(RepositoryBranchResponse.Companion::to)
        }
}