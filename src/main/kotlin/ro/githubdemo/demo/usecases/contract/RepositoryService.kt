package ro.githubdemo.demo.usecases.contract

import kotlinx.coroutines.flow.Flow
import ro.githubdemo.demo.usecases.contract.model.RepositoryData
import ro.githubdemo.demo.usecases.contract.model.Branch

interface RepositoryService {
    suspend fun getAllRepositories(username: String): Flow<RepositoryData>
    suspend fun getBranchesFor(username: String, repositoryName: String): List<Branch>
}
