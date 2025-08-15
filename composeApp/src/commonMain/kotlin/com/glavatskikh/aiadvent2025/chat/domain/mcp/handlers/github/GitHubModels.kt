package com.glavatskikh.aiadvent2025.chat.domain.mcp.handlers.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubUser(
    val login: String,
    val id: Long,
    val name: String? = null,
    val company: String? = null,
    val blog: String? = null,
    val location: String? = null,
    val email: String? = null,
    val bio: String? = null,
    @SerialName("public_repos")
    val publicRepos: Int,
    @SerialName("public_gists")
    val publicGists: Int,
    val followers: Int,
    val following: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("html_url")
    val htmlUrl: String
)

@Serializable
data class GitHubRepository(
    val id: Long,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val private: Boolean,
    val description: String? = null,
    val fork: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("pushed_at")
    val pushedAt: String? = null,
    val size: Int,
    @SerialName("stargazers_count")
    val stargazersCount: Int,
    @SerialName("watchers_count")
    val watchersCount: Int,
    val language: String? = null,
    @SerialName("forks_count")
    val forksCount: Int,
    @SerialName("open_issues_count")
    val openIssuesCount: Int,
    @SerialName("default_branch")
    val defaultBranch: String,
    @SerialName("html_url")
    val htmlUrl: String,
    val topics: List<String> = emptyList(),
    val archived: Boolean = false,
    val disabled: Boolean = false
)

@Serializable
data class GitHubError(
    val message: String,
    @SerialName("documentation_url")
    val documentationUrl: String? = null
)