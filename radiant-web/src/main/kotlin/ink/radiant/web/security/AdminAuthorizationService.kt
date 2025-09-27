package ink.radiant.web.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AdminAuthorizationService {

    fun ensureAdmin(): Authentication {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException(ADMIN_REQUIRED_MESSAGE)

        if (!authentication.isAuthenticated || !hasAdminRole(authentication.authorities)) {
            throw AccessDeniedException(ADMIN_REQUIRED_MESSAGE)
        }

        return authentication
    }

    fun currentAdminUserId(): String {
        val authentication = ensureAdmin()
        return authentication.name ?: throw AccessDeniedException(ADMIN_REQUIRED_MESSAGE)
    }

    private fun hasAdminRole(authorities: Collection<GrantedAuthority>?): Boolean {
        return authorities?.any { authority -> authority.authority == ADMIN_ROLE } == true
    }

    companion object {
        private const val ADMIN_ROLE = "ROLE_ADMIN"
        private const val ADMIN_REQUIRED_MESSAGE = "Admin role required for translation access"
    }
}
