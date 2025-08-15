package com.glavatskikh.aiadvent2025.chat.domain.mcp.handlers.github

import com.glavatskikh.aiadvent2025.core.network.HttpApiClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class GitHubApiClient(
    private val httpClient: HttpApiClient = HttpApiClient(),
) {
    companion object {
        private const val BASE_URL = "https://api.github.com"
        private const val USER_AGENT = "AIAdvent2025-KMP-App"
        private const val API_VERSION = "2022-11-28"
    }

    suspend fun getUser(username: String): Result<GitHubUser> {
        return try {
            val url = "$BASE_URL/users/$username"
            val headers = buildHeaders()

            val response = httpClient.get(url, headers)

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NotModified -> {
                    val user: GitHubUser = response.body()
                    Result.success(user)
                }

                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("User '$username' not found"))
                }

                else -> {
                    parseErrorResponse(response)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch user info: ${e.message}", e))
        }
    }

    suspend fun getUserRepositories(
        username: String,
        type: String = "all",
        sort: String = "updated",
        perPage: Int = 30,
        page: Int = 1
    ): Result<List<GitHubRepository>> {
        return try {
            val url = "$BASE_URL/users/$username/repos" +
                    "?type=$type&sort=$sort&per_page=$perPage&page=$page"
            val headers = buildHeaders()

            val response = httpClient.get(url, headers)

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NotModified -> {
                    val repos: List<GitHubRepository> = response.body()
                    Result.success(repos)
                }

                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("User '$username' not found"))
                }

                else -> {
                    parseErrorResponse(response)
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch repositories: ${e.message}", e))
        }
    }

    private fun buildHeaders(): Map<String, String> {
        return mapOf(
            "Accept" to "application/vnd.github+json",
            "X-GitHub-Api-Version" to API_VERSION,
            "User-Agent" to USER_AGENT
        )
    }

    private suspend fun <T> parseErrorResponse(response: HttpResponse): Result<T> {
        return try {
            val error: GitHubError = response.body()
            Result.failure(Exception("GitHub API Error: ${error.message}"))
        } catch (e: Exception) {
            Result.failure(
                Exception("HTTP ${response.status.value}: ${response.status.description}")
            )
        }
    }
}