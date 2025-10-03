package ink.radiant.web.config

import ink.radiant.web.security.AdminTokenAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Value("\${radiant.security.admin-tokens:test-admin-token}") private val adminTokensProperty: String,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
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
            }
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

    companion object {
        private const val DEFAULT_ADMIN_TOKEN = "test-admin-token"
        private const val ADMIN_ROLE = "ROLE_ADMIN"
    }
}
