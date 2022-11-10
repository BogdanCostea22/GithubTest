package ro.githubdemo.demo.service.model

import ro.githubdemo.demo.usecases.contract.model.Branch
import kotlin.RuntimeException

data class RepositoryBranchResponse(
    val name: String? = null,
    val commit: RepositoryBranchCommit? = null
) {
    companion object {
        fun to(repositoryBranchResponse: RepositoryBranchResponse): Branch =
            Branch(
                name = repositoryBranchResponse.name ?: throw RuntimeException("Invalid value!"),
                lastCommitSha = repositoryBranchResponse.commit?.sha ?: throw RuntimeException("Invalid value!")
            )
    }
}

data class RepositoryBranchCommit(
    val url: String? = null,
    val sha: String? = null,
)