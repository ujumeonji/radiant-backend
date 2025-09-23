package ink.radiant.web.controller

import ink.radiant.web.dto.OAuthLoginRequest
import ink.radiant.web.dto.OAuthLoginResponse
import ink.radiant.web.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @GetMapping("/oauth2/authorization/{provider}")
    fun getAuthorizationUrl(@PathVariable provider: String): ResponseEntity<Map<String, String>> {
        val authUrl = authService.getAuthorizationUrl(provider)
        return ResponseEntity.ok(mapOf("authUrl" to authUrl))
    }

    @PostMapping("/oauth2/callback/{provider}")
    fun handleOAuthCallback(
        @PathVariable provider: String,
        @RequestBody request: OAuthLoginRequest,
    ): ResponseEntity<OAuthLoginResponse> {
        val response = authService.processOAuthLogin(provider, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") refreshToken: String): ResponseEntity<OAuthLoginResponse> {
        val response = authService.refreshToken(refreshToken)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") accessToken: String): ResponseEntity<Map<String, String>> {
        authService.logout(accessToken)
        return ResponseEntity.ok(mapOf("message" to "Successfully logged out"))
    }
}
