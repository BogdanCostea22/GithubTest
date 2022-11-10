package ro.githubdemo.demo.service.model

import ro.githubdemo.demo.usecases.contract.model.RepositoryData

data class RepositoryResponse(
    val id: String? = null,
    val name: String? = null,
    val owner: OwnerResponse? = null
) {
    companion object {
        fun to(repositoryResponse: RepositoryResponse): RepositoryData =
            RepositoryData(
                name = repositoryResponse.name ?: throw RuntimeException("Invalid response!"),
                login = repositoryResponse.owner?.login ?: throw RuntimeException("Invalid response!")
            )
    }
}


data class OwnerResponse(
    val login: String? = null
)
