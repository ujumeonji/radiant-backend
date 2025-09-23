package ink.radiant.web.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.Key
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class JwtService {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.access-token.expiration:3600}")
    private var accessTokenExpiration: Long = 3600

    @Value("\${jwt.refresh-token.expiration:86400}")
    private var refreshTokenExpiration: Long = 86400

    private val invalidatedTokens = ConcurrentHashMap<String, Long>()

    private fun getSigningKey(): Key {
        val keyBytes = secret.toByteArray()
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateAccessToken(userId: String, now: OffsetDateTime): String {
        val issuedAt = Date.from(now.toInstant())
        val expiration = Date(issuedAt.time + accessTokenExpiration * 1000)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .claim("type", "access")
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }

    fun generateRefreshToken(userId: String, now: OffsetDateTime): String {
        val issuedAt = Date.from(now.toInstant())
        val expiration = Date(issuedAt.time + refreshTokenExpiration * 1000)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(issuedAt)
            .setExpiration(expiration)
            .claim("type", "refresh")
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }

    fun validateAccessToken(token: String): String {
        if (isTokenInvalidated(token)) {
            throw RuntimeException("Token has been invalidated")
        }

        val claims = parseToken(token)
        val tokenType = claims["type"] as? String

        if (tokenType != "access") {
            throw RuntimeException("Invalid token type")
        }

        return claims.subject
    }

    fun validateRefreshToken(token: String): String {
        if (isTokenInvalidated(token)) {
            throw RuntimeException("Token has been invalidated")
        }

        val claims = parseToken(token)
        val tokenType = claims["type"] as? String

        if (tokenType != "refresh") {
            throw RuntimeException("Invalid token type")
        }

        return claims.subject
    }

    fun invalidateToken(token: String) {
        val claims = parseToken(token)
        val expiration = claims.expiration.time
        invalidatedTokens[token] = expiration

        cleanupExpiredTokens()
    }

    private fun parseToken(token: String): Claims = try {
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
    } catch (e: Exception) {
        throw RuntimeException("Invalid token: ${e.message}")
    }

    private fun isTokenInvalidated(token: String): Boolean {
        return invalidatedTokens.containsKey(token)
    }

    private fun cleanupExpiredTokens() {
        val now = System.currentTimeMillis()
        invalidatedTokens.entries.removeIf { it.value < now }
    }
}
