package com.glavatskikh.aiadvent2025.chat.domain.mcp.handlers.github

import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.BaseMCPHandler
import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.MCPSchema
import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.MCPTool
import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.MCPToolResult
import com.glavatskikh.aiadvent2025.chat.domain.mcp.core.SchemaProperty
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class GitHubMCPHandler(
    private val apiClient: GitHubApiClient = GitHubApiClient()
) : BaseMCPHandler() {

    override val name = "github"
    override val description = "GitHub API integration for user and repository information"

    override val tools = listOf(
        MCPTool(
            name = "get_user_info",
            description = "Get information about a GitHub user",
            inputSchema = MCPSchema(
                type = "object",
                properties = mapOf(
                    "username" to SchemaProperty(
                        type = "string",
                        description = "GitHub username to get information about"
                    )
                ),
                required = listOf("username")
            )
        ),
        MCPTool(
            name = "list_repositories",
            description = "List repositories for a GitHub user",
            inputSchema = MCPSchema(
                type = "object",
                properties = mapOf(
                    "username" to SchemaProperty(
                        type = "string",
                        description = "GitHub username whose repositories to list"
                    ),
                    "type" to SchemaProperty(
                        type = "string",
                        description = "Type of repositories to list (all, owner, member)"
                    ),
                    "sort" to SchemaProperty(
                        type = "string",
                        description = "Sort order for repositories (created, updated, pushed, full_name)"
                    ),
                    "per_page" to SchemaProperty(
                        type = "integer",
                        description = "Number of repositories per page (max 100)"
                    ),
                    "page" to SchemaProperty(
                        type = "integer",
                        description = "Page number for pagination"
                    )
                ),
                required = listOf("username")
            )
        )
    )

    override suspend fun executeInternal(
        toolName: String,
        arguments: Map<String, Any?>
    ): MCPToolResult {
        return when (toolName) {
            "get_user_info" -> getUserInfo(arguments)
            "list_repositories" -> listRepositories(arguments)
            else -> MCPToolResult.error("Unknown tool: $toolName")
        }
    }

    private suspend fun getUserInfo(arguments: Map<String, Any?>): MCPToolResult {
        val username = arguments["username"] as String

        return apiClient.getUser(username).fold(
            onSuccess = { user ->
                val json = buildJsonObject {
                    put("login", JsonPrimitive(user.login))
                    put("name", JsonPrimitive(user.name ?: ""))
                    put("bio", JsonPrimitive(user.bio ?: ""))
                    put("company", JsonPrimitive(user.company ?: ""))
                    put("location", JsonPrimitive(user.location ?: ""))
                    put("publicRepos", JsonPrimitive(user.publicRepos))
                    put("followers", JsonPrimitive(user.followers))
                    put("following", JsonPrimitive(user.following))
                    put("createdAt", JsonPrimitive(user.createdAt))
                    put("htmlUrl", JsonPrimitive(user.htmlUrl))
                }
                MCPToolResult.success(json.toString())
            },
            onFailure = { error ->
                MCPToolResult.error(error.message ?: "Failed to get user info")
            }
        )
    }

    private suspend fun listRepositories(arguments: Map<String, Any?>): MCPToolResult {
        val username = arguments["username"] as String
        val type = (arguments["type"] as? String) ?: "all"
        val sort = (arguments["sort"] as? String) ?: "updated"
        val perPage = (arguments["per_page"] as? Number)?.toInt() ?: 30
        val page = (arguments["page"] as? Number)?.toInt() ?: 1

        return apiClient.getUserRepositories(
            username = username,
            type = type,
            sort = sort,
            perPage = perPage.coerceIn(1, 100),
            page = page
        ).fold(
            onSuccess = { repos ->
                val json = buildJsonObject {
                    put("username", JsonPrimitive(username))
                    put("count", JsonPrimitive(repos.size))
                    put("repositories", buildJsonArray {
                        repos.forEach { repo ->
                            add(buildJsonObject {
                                put("name", JsonPrimitive(repo.name))
                                put("description", JsonPrimitive(repo.description ?: ""))
                                put("language", JsonPrimitive(repo.language ?: ""))
                                put("stars", JsonPrimitive(repo.stargazersCount))
                                put("forks", JsonPrimitive(repo.forksCount))
                                put("updatedAt", JsonPrimitive(repo.updatedAt))
                                put("htmlUrl", JsonPrimitive(repo.htmlUrl))
                            })
                        }
                    })
                }
                MCPToolResult.success(json.toString())
            },
            onFailure = { error ->
                MCPToolResult.error(error.message ?: "Failed to list repositories")
            }
        )
    }
}