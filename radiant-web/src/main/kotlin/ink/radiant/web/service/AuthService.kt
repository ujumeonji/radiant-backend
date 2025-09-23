package ink.radiant.web.service

import ink.radiant.command.service.UserCommandService
import ink.radiant.core.domain.model.OAuthUser
import ink.radiant.web.dto.*
import ink.radiant.web.service.oauth.GitHubOAuthService
import ink.radiant.web.service.oauth.GoogleOAuthService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class AuthService(
    private val gitHubOAuthService: GitHubOAuthService,
    private val googleOAuthService: GoogleOAuthService,
    private val jwtService: JwtService,
    private val userCommandService: UserCommandService,
    @Value("\${jwt.access-token.expiration:3600}")
    private var accessTokenExpiration: Long = 3600,
    @Value("\${jwt.refresh-token.expiration:86400}")
    private var refreshTokenExpiration: Long = 86400,
) {

    fun getAuthorizationUrl(provider: String): String = when (provider.lowercase()) {
        "github" -> gitHubOAuthService.getAuthorizationUrl()
        "google" -> googleOAuthService.getAuthorizationUrl()
        else -> throw IllegalArgumentException("Unsupported OAuth provider: $provider")
    }

    fun processOAuthLogin(provider: String, request: OAuthLoginRequest): OAuthLoginResponse = try {
        val oauthUser = when (provider.lowercase()) {
            "github" -> {
                val accessToken = gitHubOAuthService.exchangeCodeForToken(request.code, request.redirectUri)
                val githubUser = gitHubOAuthService.getUserInfo(accessToken)
                UserDto(
                    id = githubUser.id.toString(),
                    username = githubUser.login,
                    name = githubUser.name ?: githubUser.login,
                    email = githubUser.email ?: "${githubUser.login}@github.local",
                    avatarUrl = githubUser.avatarUrl,
                    provider = "github",
                )
            }
            "google" -> {
                val accessToken = googleOAuthService.exchangeCodeForToken(request.code, request.redirectUri)
                val googleUser = googleOAuthService.getUserInfo(accessToken)
                UserDto(
                    id = googleUser.id,
                    username = googleUser.email.substringBefore(EMAIL_DELIMITER),
                    name = googleUser.name,
                    email = googleUser.email,
                    avatarUrl = googleUser.picture,
                    provider = "google",
                )
            }
            else -> throw IllegalArgumentException("Unsupported OAuth provider: $provider")
        }

        val user = userCommandService.findOrCreateUser(
            OAuthUser(
                id = oauthUser.id,
                username = oauthUser.username,
                name = oauthUser.name,
                email = oauthUser.email,
                avatarUrl = oauthUser.avatarUrl,
                provider = oauthUser.provider,
            ),
        )

        val now = OffsetDateTime.now()
        val accessToken = jwtService.generateAccessToken(user.id, now)
        val refreshToken = jwtService.generateRefreshToken(user.id, now)

        OAuthLoginResponse(
            success = true,
            message = "Login successful",
            user = UserDto(
                id = user.id,
                username = user.username,
                name = user.name,
                email = user.email,
                avatarUrl = user.avatarUrl,
                provider = user.provider,
            ),
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = accessTokenExpiration,
        )
    } catch (e: Exception) {
        OAuthLoginResponse(
            success = false,
            message = "Login failed: ${e.message}",
        )
    }

    fun refreshToken(refreshToken: String): OAuthLoginResponse = try {
        val token = refreshToken.removePrefix(TOKEN_PREFIX)
        val userId = jwtService.validateRefreshToken(token)

        val now = OffsetDateTime.now()
        val newAccessToken = jwtService.generateAccessToken(userId, now)
        val newRefreshToken = jwtService.generateRefreshToken(userId, now)

        OAuthLoginResponse(
            success = true,
            message = "Token refreshed successfully",
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = refreshTokenExpiration,
        )
    } catch (e: Exception) {
        OAuthLoginResponse(
            success = false,
            message = "Token refresh failed: ${e.message}",
        )
    }

    fun logout(accessToken: String) {
        val token = accessToken.removePrefix(TOKEN_PREFIX)
        jwtService.invalidateToken(token)
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthService::class.java)
        private const val TOKEN_PREFIX = "Bearer "
        private const val EMAIL_DELIMITER = "@"
    }
}
