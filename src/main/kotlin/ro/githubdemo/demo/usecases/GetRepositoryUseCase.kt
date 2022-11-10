package ro.githubdemo.demo.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import ro.githubdemo.demo.controllers.CustomAppException
import ro.githubdemo.demo.usecases.contract.RepositoryService


@Component
class GetRepositoryUseCase(
    private val repositoryService: RepositoryService
) : UseCaseStructure<String, String, RepositoryWithBranches>() {
    override fun validateRequest(headers: Map<String, String>) {
        when {
            headers["content-type"] != MediaType.APPLICATION_JSON_VALUE -> throw CustomAppException.InvalidHeader()
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
