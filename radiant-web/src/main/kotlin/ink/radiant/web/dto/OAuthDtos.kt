package ink.radiant.web.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthLoginRequest(
    val code: String,
    val state: String? = null,
    @JsonProperty("redirect_uri")
    val redirectUri: String,
)

data class UserDto(
    val id: String,
    val username: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val provider: String,
)

data class OAuthLoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserDto? = null,
    @JsonProperty("access_token")
    val accessToken: String? = null,
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @JsonProperty("expires_in")
    val expiresIn: Long? = null,
)

data class GitHubUser(
    val id: Long,
    val login: String,
    val name: String?,
    @JsonProperty("avatar_url")
    val avatarUrl: String?,
    val email: String?,
    val bio: String?,
    val location: String?,
    @JsonProperty("html_url")
    val htmlUrl: String?,
    @JsonProperty("public_repos")
    val publicRepos: Int?,
    val followers: Int?,
    val following: Int?,
)

data class GoogleUser(
    val id: String,
    val email: String,
    @JsonProperty("verified_email")
    val verifiedEmail: Boolean,
    val name: String,
    @JsonProperty("given_name")
    val givenName: String,
    @JsonProperty("family_name")
    val familyName: String,
    val picture: String?,
    val locale: String?,
)

data class OAuthTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String,
    val scope: String?,
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    @JsonProperty("expires_in")
    val expiresIn: Long? = null,
)
