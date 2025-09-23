package ink.radiant.web.service.oauth

import ink.radiant.web.dto.GitHubUser
import ink.radiant.web.dto.OAuthTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class GitHubOAuthService(
    private val restTemplate: RestTemplate = RestTemplate(),
) {

    @Value("\${spring.security.oauth2.client.registration.github.client-id}")
    private lateinit var clientId: String

    @Value("\${spring.security.oauth2.client.registration.github.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${spring.security.oauth2.client.registration.github.redirect-uri}")
    private lateinit var redirectUri: String

    fun getAuthorizationUrl(): String {
        val scope = "user:email"
        val state = generateState()

        return "$GITHUB_AUTHORIZE_URL?" +
            "client_id=${URLEncoder.encode(clientId, StandardCharsets.UTF_8)}&" +
            "redirect_uri=${URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)}&" +
            "scope=${URLEncoder.encode(scope, StandardCharsets.UTF_8)}&" +
            "state=${URLEncoder.encode(state, StandardCharsets.UTF_8)}"
    }

    fun exchangeCodeForToken(code: String, redirectUri: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Accept", "application/json")

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", clientId)
        body.add("client_secret", clientSecret)
        body.add("code", code)
        body.add("redirect_uri", redirectUri)

        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity(GITHUB_TOKEN_URL, request, OAuthTokenResponse::class.java)

        return response.body?.accessToken ?: throw RuntimeException("Failed to get access token from GitHub")
    }

    fun getUserInfo(accessToken: String): GitHubUser {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.set("Accept", "application/vnd.github.v3+json")

        val request = HttpEntity<String>(headers)
        val userResponse = restTemplate.exchange(GITHUB_USER_URL, HttpMethod.GET, request, GitHubUser::class.java)

        var user = userResponse.body ?: throw RuntimeException("Failed to get user info from GitHub")

        if (user.email == null) {
            user = user.copy(email = getPrimaryEmail(accessToken))
        }

        return user
    }

    private fun getPrimaryEmail(accessToken: String): String {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.set("Accept", "application/vnd.github.v3+json")

        val request = HttpEntity<String>(headers)
        val emailResponse = restTemplate.exchange(
            GITHUB_USER_EMAIL_URL,
            HttpMethod.GET,
            request,
            Array<GitHubEmail>::class.java,
        )

        val emails = emailResponse.body ?: emptyArray()
        return emails.find { it.primary && it.verified }?.email
            ?: emails.firstOrNull()?.email
            ?: throw RuntimeException("No email found for GitHub user")
    }

    private fun generateState(): String {
        return java.util.UUID.randomUUID().toString()
    }

    data class GitHubEmail(
        val email: String,
        val verified: Boolean,
        val primary: Boolean,
        val visibility: String?,
    )

    companion object {
        private const val GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize"
        private const val GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token"
        private const val GITHUB_USER_URL = "https://api.github.com/user"
        private const val GITHUB_USER_EMAIL_URL = "https://api.github.com/user/emails"
    }
}
