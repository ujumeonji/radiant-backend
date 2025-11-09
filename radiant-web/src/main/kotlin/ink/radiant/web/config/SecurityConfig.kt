package ink.radiant.web.config

import com.fasterxml.jackson.databind.ObjectMapper
import ink.radiant.web.security.AdminTokenAuthenticationFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.Instant

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class SecurityConfig(
    @param:Value("\${radiant.security.admin-tokens:test-admin-token}") private val adminTokensProperty: String,
    private val oauth2UserService: OAuth2UserService<OAuth2UserRequest, OAuth2User>,
) {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        encoder: JwtEncoder,
        objectMapper: ObjectMapper,
        @Value("\${jwt.access-token.expiration}") expiration: Long,
    ): SecurityFilterChain {
        val adminTokens = parseTokens(adminTokensProperty)

        http
            .cors { }
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .oauth2Login { authLoginConfigurer ->
                authLoginConfigurer.userInfoEndpoint {
                    it.userService(oauth2UserService)
                }
                authLoginConfigurer.redirectionEndpoint {
                    it.baseUri("/api/auth/oauth2/callback/*")
                }
                authLoginConfigurer.successHandler(
                    OAuth2AuthenticationSuccessHandler(
                        encoder,
                        objectMapper,
                        expiration,
                    ),
                )
            }
            .oauth2ResourceServer { it.jwt { } }
            .logout { it.disable() }
            .sessionManagement { manager ->
                manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { registry ->
                registry
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/graphql", "/graphiql", "/vendor/**").permitAll()
                    .anyRequest().permitAll()
            }
            .addFilterBefore(
                AdminTokenAuthenticationFilter(adminTokens, ADMIN_ROLE),
                UsernamePasswordAuthenticationFilter::class.java,
            )

        return http.build()
    }

    private fun parseTokens(rawTokens: String): Set<String> {
        return rawTokens.split(",")
            .map(String::trim)
            .filter { it.isNotEmpty() }
            .toSet()
            .ifEmpty { setOf(DEFAULT_ADMIN_TOKEN) }
    }

    private class OAuth2AuthenticationSuccessHandler(
        private val encoder: JwtEncoder,
        private val objectMapper: ObjectMapper,
        private val expiration: Long,
    ) : SimpleUrlAuthenticationSuccessHandler() {

        override fun onAuthenticationSuccess(
            request: HttpServletRequest,
            response: HttpServletResponse,
            authentication: Authentication,
        ) {
            val oAuth2User = authentication.principal as OAuth2User

            val accountId = oAuth2User.getRequiredAttribute<String>(RADIANT_ACCOUNT_ID_ATTRIBUTE)
            val email = oAuth2User.getRequiredAttribute<String>(RADIANT_ACCOUNT_EMAIL_ATTRIBUTE)

            val jwtToken = generateToken(accountId)

            response.contentType = CONTENT_TYPE_JSON
            response.characterEncoding = RESPONSE_ENCODING_UTF8

            val responseData = mutableMapOf(
                RESPONSE_TOKEN_KEY to jwtToken,
                RESPONSE_TYPE_KEY to TOKEN_TYPE_BEARER,
                RESPONSE_ACCOUNT_ID_KEY to accountId,
            )

            response.writer.write(objectMapper.writeValueAsString(responseData))
        }

        private fun generateToken(accountId: String): String {
            val now = Instant.now()
            val expiresInSeconds = expiration

            val claimsBuilder = JwtClaimsSet.builder()
                .issuer(JWT_ISSUER_SELF)
                .subject(accountId)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .claim(JWT_ACCOUNT_ID_CLAIM, accountId)
            val claims = claimsBuilder.build()
            val header = JwsHeader.with(MacAlgorithm.HS256).build()

            return encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        }

        private fun <T> OAuth2User.getRequiredAttribute(name: String): T =
            getAttribute<T>(name) ?: error("$MISSING_ATTRIBUTE_MESSAGE_PREFIX$name")

        private companion object {
            private const val CONTENT_TYPE_JSON = "application/json"
            private const val RESPONSE_ENCODING_UTF8 = "UTF-8"
            private const val RESPONSE_TOKEN_KEY = "token"
            private const val RESPONSE_TYPE_KEY = "type"
            private const val RESPONSE_ACCOUNT_ID_KEY = "accountId"
            private const val TOKEN_TYPE_BEARER = "Bearer"
            private const val JWT_ISSUER_SELF = "self"
            private const val JWT_ACCOUNT_ID_CLAIM = "accountId"
            private const val MISSING_ATTRIBUTE_MESSAGE_PREFIX = "Missing OAuth2 attribute: "
        }
    }

    companion object {
        private const val DEFAULT_ADMIN_TOKEN = "test-admin-token"
        private const val ADMIN_ROLE = "ROLE_ADMIN"
        private const val RADIANT_ACCOUNT_ID_ATTRIBUTE = "radiantAccountId"
        private const val RADIANT_ACCOUNT_EMAIL_ATTRIBUTE = "radiantAccountEmail"
        private const val RADIANT_ACCOUNT_NAME_ATTRIBUTE = "radiantAccountName"
    }
}
