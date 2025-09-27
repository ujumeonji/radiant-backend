package ink.radiant.web.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class AdminTokenAuthenticationFilter(
    private val adminTokens: Set<String>,
    private val adminRole: String,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header?.startsWith(BEARER_PREFIX) == true && SecurityContextHolder.getContext().authentication == null) {
            val token = header.removePrefix(BEARER_PREFIX).trim()
            if (adminTokens.contains(token)) {
                val authentication = UsernamePasswordAuthenticationToken(
                    "admin",
                    null,
                    listOf(SimpleGrantedAuthority(adminRole)),
                ).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
