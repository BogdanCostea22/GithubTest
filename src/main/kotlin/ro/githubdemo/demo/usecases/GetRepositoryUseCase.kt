package ro.githubdemo.demo.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import ro.githubdemo.demo.controllers.CustomAppException
import ro.githubdemo.demo.service.RepositoryDetails
import ro.githubdemo.demo.service.RepositoryService


@Component
class GetRepositoryUseCase(
    private val repositoryService: RepositoryService
) : UseCaseStructure<String, String, RepositoryWithBranches>() {
    override fun validateRequest(headers: Map<String, String>) {
        when {
            headers[HttpHeaders.CONTENT_TYPE] != MediaType.APPLICATION_JSON_VALUE -> throw CustomAppException.InvalidHeader()
        }
    }

    override suspend fun executeBusinessLogicInternal(param: String, requestBody: String?): Flow<RepositoryWithBranches> {
        return repositoryService.getAllRepositories(param).map {
            try {
                val branches = repositoryService.getBranchesFor(username = param, it.name)
                RepositoryWithBranches(
                    name = it.name,
                    login = it.login,
                    branches = branches
                )
            } catch (exc: Exception) {
                throw  exc
            }
        }
    }
}

data class RepositoryWithBranches(
    val name: String,
    val login: String,
    val branches: List<RepositoryDetails>
)