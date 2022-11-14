package ro.githubdemo.demo.unit.fixture

import ro.githubdemo.demo.service.model.OwnerResponse
import ro.githubdemo.demo.service.model.RepositoryBranchCommit
import ro.githubdemo.demo.service.model.RepositoryBranchResponse
import ro.githubdemo.demo.service.model.RepositoryResponse
import kotlin.random.Random

object RepositoryFixture {
    const val REPOSITORY_NAME = "REPOSITORY Name"
    const val REPOSITORY_BRANCH_NAME = "REPOSITORY_BRANCH_NAME"
    const val REPOSITORY_BRANCH_URL = "repository/branch/url"
    const val REPOSITORY_BRANCH_SHA = "branch/sha"
    const val LOGIN_NAME = "login_username"

    fun createRepositoryResponse(): RepositoryResponse {
        val repositoryOwner = OwnerResponse(
            login = LOGIN_NAME
        )
        val repositoryResponse = RepositoryResponse(
            id = Random.nextInt().toString(),
            name = REPOSITORY_NAME,
            owner = repositoryOwner
        )
        return repositoryResponse
    }

    fun createRepositoryBranchesResponse(): RepositoryBranchResponse =
        RepositoryBranchResponse(
            name = REPOSITORY_BRANCH_NAME,
            commit = RepositoryBranchCommit(
                url = REPOSITORY_BRANCH_URL,
                sha = REPOSITORY_BRANCH_SHA
            )
        )
}