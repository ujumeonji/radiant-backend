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

            val email = oAuth2User.getAttribute<String>("email")
                ?: oAuth2User.getAttribute("login")
            val profileUrl = oAuth2User.getAttribute<String>("avatar_url")
                ?: oAuth2User.getAttribute("picture")

            val jwtToken = generateToken(email, profileUrl)

            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"

            val responseData = mapOf(
                "token" to jwtToken,
                "type" to "Bearer",
                "email" to email,
                "profileUrl" to profileUrl,
            )

            response.writer.write(objectMapper.writeValueAsString(responseData))
        }

        private fun generateToken(email: String, profileUrl: String): String {
            val now = Instant.now()
            val expiresInSeconds = expiration

            val claims = JwtClaimsSet.builder()
                .issuer("self")
                .subject(email)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .claim("email", email)
                .claim("profileUrl", profileUrl)
                .build()
            val header = JwsHeader.with(MacAlgorithm.HS256).build()

            return encoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        }
    }

    private fun parseTokens(rawTokens: String): Set<String> {
        return rawTokens.split(",")
            .map(String::trim)
            .filter { it.isNotEmpty() }
            .toSet()
            .ifEmpty { setOf(DEFAULT_ADMIN_TOKEN) }
    }

    companion object {
        private const val DEFAULT_ADMIN_TOKEN = "test-admin-token"
        private const val ADMIN_ROLE = "ROLE_ADMIN"
    }
}
