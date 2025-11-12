package ink.radiant.web.security

import ink.radiant.command.service.UserCommandService
import ink.radiant.core.domain.model.OAuthAccount
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class CustomOidcUserService(
    private val userCommandService: UserCommandService,
) : OAuth2UserService<OidcUserRequest, OidcUser> {

    private val delegate = OidcUserService()

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = delegate.loadUser(userRequest)
        val registrationId = userRequest.clientRegistration.registrationId.lowercase(Locale.ROOT)

        val oauthAccount = mapToOAuthAccount(registrationId, oidcUser)
        val account = userCommandService.findOrCreateUser(oauthAccount)

        val claims = oidcUser.userInfo?.claims?.toMutableMap() ?: mutableMapOf()
        val principalAttributeName =
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName
                ?: oidcUser.name
        claims[principalAttributeName] = account.id
        val updatedUserInfo = OidcUserInfo(claims)

        return DefaultOidcUser(
            oidcUser.authorities.withDefaultRole(),
            oidcUser.idToken,
            updatedUserInfo,
            principalAttributeName,
        )
    }

    private fun Collection<GrantedAuthority>.withDefaultRole(): Collection<GrantedAuthority> =
        if (any { it.authority == DEFAULT_USER_ROLE }) this else this + SimpleGrantedAuthority(DEFAULT_USER_ROLE)

    private fun mapToOAuthAccount(registrationId: String, user: OAuth2User): OAuthAccount = when (registrationId) {
        "github" -> mapGitHubUser(user)
        "google" -> mapGoogleUser(user)
        else -> throw oauthError("Unsupported OAuth2 provider: $registrationId")
    }

    private fun mapGitHubUser(user: OAuth2User): OAuthAccount {
        val id = "${user.getRequiredAttribute<Int>("id", "GitHub")}"
        val login = user.getRequiredAttribute<String>("login", "GitHub")
        val email = user.getAttribute<String>("email")?.takeIf(String::isNotBlank) ?: "$login@github.local"
        val name = user.getAttribute<String>("name")?.takeIf(String::isNotBlank) ?: login
        val avatar = user.getAttribute<String>("avatar_url")

        return OAuthAccount(
            id = id,
            username = login,
            name = name,
            email = email,
            avatarUrl = avatar,
            provider = "github",
        )
    }

    private fun mapGoogleUser(user: OAuth2User): OAuthAccount {
        val id = user.getAttribute<String>("sub")
            ?: user.name
        val email = user.getRequiredAttribute<String>("email", "Google")
        val username = email.substringBefore(EMAIL_DELIMITER)
        val displayName = user.getAttribute<String>("name")?.takeIf(String::isNotBlank) ?: username
        val avatar = user.getAttribute<String>("picture")

        return OAuthAccount(
            id = id,
            username = username,
            name = displayName,
            email = email,
            avatarUrl = avatar,
            provider = "google",
        )
    }

    private inline fun <reified T> OAuth2User.getAttribute(name: String): T? = attributes[name] as? T

    private inline fun <reified T> OAuth2User.getRequiredAttribute(name: String, provider: String): T =
        getAttribute<T>(name) ?: throw oauthError("$provider response missing $name attribute")

    private fun oauthError(message: String): OAuth2AuthenticationException =
        OAuth2AuthenticationException(OAuth2Error(OAUTH_ERROR_CODE, message, null))

    companion object {
        private const val EMAIL_DELIMITER = "@"
        private const val DEFAULT_USER_ROLE = "ROLE_USER"
        private const val OAUTH_ERROR_CODE = "invalid_oauth_user"
    }
}
