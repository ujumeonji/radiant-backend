package ink.radiant.web.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration(proxyBeanMethods = false)
class JwtConfig(
    @param:Value("\${jwt.secret}") private val secret: String,
) {
    @Bean
    fun jwtEncoder(): JwtEncoder {
        val key = getSecretKey()
        return NimbusJwtEncoder(ImmutableSecret(key))
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val key = getSecretKey()
        return NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }

    private fun getSecretKey(): SecretKey {
        val keyBytes = secret.toByteArray(Charsets.UTF_8)
        require(keyBytes.size >= 32) { "JWT secret must be at least 256 bits (32 bytes)" }
        return SecretKeySpec(keyBytes, "HmacSHA256")
    }
}
