package ro.githubdemo.demo.service

import kotlinx.coroutines.flow.Flow

interface RepositoryService {
    suspend fun getAllRepositories(username: String): Flow<RepositoryData>
    suspend fun getBranchesFor(username: String, repositoryName: String): List<RepositoryDetails>
}
