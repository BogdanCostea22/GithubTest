package ro.githubdemo.demo.usecases.contract.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Branch(
    @JsonProperty("name")
    val name: String,
    @JsonProperty("last_commit_sha")
    val lastCommitSha: String
)
