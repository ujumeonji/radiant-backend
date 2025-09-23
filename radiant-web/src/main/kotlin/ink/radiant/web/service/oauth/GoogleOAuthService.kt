package ink.radiant.web.service.oauth

import ink.radiant.web.dto.GoogleUser
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
class GoogleOAuthService(
    private val restTemplate: RestTemplate,
) {

    @Value("\${spring.security.oauth2.client.registration.google.client-id}")
    private lateinit var clientId: String

    @Value("\${spring.security.oauth2.client.registration.google.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${spring.security.oauth2.client.registration.google.redirect-uri}")
    private lateinit var redirectUri: String

    fun getAuthorizationUrl(): String {
        val scope = "openid email profile"
        val state = generateState()

        return "$GOOGLE_AUTHORIZE_URL?" +
            "client_id=${URLEncoder.encode(clientId, StandardCharsets.UTF_8)}&" +
            "redirect_uri=${URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)}&" +
            "scope=${URLEncoder.encode(scope, StandardCharsets.UTF_8)}&" +
            "response_type=code&" +
            "state=${URLEncoder.encode(state, StandardCharsets.UTF_8)}&" +
            "access_type=offline&" +
            "prompt=consent"
    }

    fun exchangeCodeForToken(code: String, redirectUri: String): String {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("client_id", clientId)
        body.add("client_secret", clientSecret)
        body.add("code", code)
        body.add("grant_type", "authorization_code")
        body.add("redirect_uri", redirectUri)

        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity(GOOGLE_TOKEN_URL, request, OAuthTokenResponse::class.java)

        return response.body?.accessToken ?: throw RuntimeException("Failed to get access token from Google")
    }

    fun getUserInfo(accessToken: String): GoogleUser {
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val request = HttpEntity<String>(headers)
        val response = restTemplate.exchange(GOOGLE_USER_URL, HttpMethod.GET, request, GoogleUser::class.java)

        return response.body ?: throw RuntimeException("Failed to get user info from Google")
    }

    private fun generateState(): String {
        return java.util.UUID.randomUUID().toString()
    }

    companion object {
        private const val GOOGLE_AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
        private const val GOOGLE_USER_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
    }
}
