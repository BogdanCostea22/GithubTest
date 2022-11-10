package ro.githubdemo.demo.usecases

import ro.githubdemo.demo.usecases.contract.model.Branch

data class RepositoryWithBranches(
    val name: String,
    val login: String,
    val branches: List<Branch>
)