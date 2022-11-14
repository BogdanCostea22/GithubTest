package ro.githubdemo.demo.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import ro.githubdemo.demo.controllers.CustomAppException
import ro.githubdemo.demo.usecases.contract.RepositoryService


@Component
class GetRepositoryUseCase(
    private val repositoryService: RepositoryService
) : UseCaseStructure<String, RepositoryWithBranches>() {
    companion object {
        private val log = LoggerFactory.getLogger(GetRepositoryUseCase::class.java)
    }

    override fun validateRequest(headers: HttpHeaders?) {
        headers?.accept?.firstOrNull { it == MediaType.APPLICATION_JSON }
            ?: run {
                log.debug("Invalid header!")
                throw CustomAppException.InvalidHeader()
            }

    }

    override suspend fun executeBusinessLogicInternal(param: String): Flow<RepositoryWithBranches> {
        return repositoryService.getAllRepositories(param).map { repository ->
            log.debug("Fetching branches for repository ${repository.name}")

            val branches = repositoryService.getBranchesFor(username = param, repository.name)
            RepositoryWithBranches(
                name = repository.name,
                login = repository.login,
                branches = branches
            )
        }
    }
}
