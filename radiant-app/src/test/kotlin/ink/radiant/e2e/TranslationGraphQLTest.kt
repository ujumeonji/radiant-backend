package ink.radiant.e2e

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ink.radiant.base.BaseGraphQLTest
import ink.radiant.config.TestTranslationConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.reactive.server.WebTestClient

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(TestTranslationConfiguration::class)
class TranslationGraphQLTest : BaseGraphQLTest() {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val adminToken = "test-admin-token"

    @Test
    fun `translateToKorean 뮤테이션은 번역 세션을 비동기로 생성한다`() {
        val mutation = """
            mutation TranslateEnglish {
                translateToKorean(
                    input: {
                        sourceText: "Hello world! This is a test translation."
                    }
                ) {
                    __typename
                    ... on TranslationQueued {
                        sessionId
                        status
                    }
                    ... on TranslationError {
                        type
                        message
                        code
                    }
                }
            }
        """.trimIndent()

        val result = executeAuthorizedGraphQLQuery(mutation)
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()

        val json = parseJson(result.responseBody)
        val payload = json.path("data").path("translateToKorean")
        assertEquals("TranslationQueued", payload.path("__typename").asText())
        val sessionId = payload.path("sessionId").asText()
        assertTrue(sessionId.isNotBlank())
        assertEquals("IN_PROGRESS", payload.path("status").asText())
    }

    @Test
    fun `translationSession 쿼리는 번역 완료 후 세션 정보를 반환한다`() {
        val mutation = """
            mutation StartTranslation {
                translateToKorean(
                    input: {
                        sourceText: "Hello world! This is a test translation."
                    }
                ) {
                    ... on TranslationQueued {
                        sessionId
                    }
                    ... on TranslationError {
                        type
                        message
                    }
                }
            }
        """.trimIndent()

        val sessionId = extractSessionId(mutation)
        waitForTranslationCompletion(sessionId)

        val query = """
            query GetTranslationSession {
                translationSession(sessionId: "$sessionId") {
                    id
                    status
                    textLength
                    sourceLanguage {
                        code
                    }
                    createdAt
                    completedAt
                }
            }
        """.trimIndent()

        executeAuthorizedGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.translationSession.id").isEqualTo(sessionId)
            .jsonPath("$.data.translationSession.status").isEqualTo("COMPLETED")
            .jsonPath("$.data.translationSession.textLength").isEqualTo(40)
            .jsonPath("$.data.translationSession.sourceLanguage.code").isEqualTo("EN")
            .jsonPath("$.data.translationSession.createdAt").isNotEmpty
            .jsonPath("$.data.translationSession.completedAt").isNotEmpty
    }

    @Test
    fun `translateToKorean 뮤테이션은 관리자 인증이 필요하다`() {
        val mutation = """
            mutation UnauthorizedTranslate {
                translateToKorean(
                    input: {
                        sourceText: "Hello"
                    }
                ) {
                    __typename
                    ... on TranslationError {
                        type
                        message
                        code
                    }
                }
            }
        """.trimIndent()

        webTestClient.post()
            .uri("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("query" to mutation))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.errors[0].message").value<String> { message ->
                assertTrue(message.contains("Admin role required"))
            }
    }

    private fun extractSessionId(mutation: String): String {
        val result = executeAuthorizedGraphQLQuery(mutation)
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()

        val body = result.responseBody
        assertNotNull(body)
        val json = objectMapper.readTree(body)
        val sessionId = json.path("data").path("translateToKorean").path("sessionId").asText()
        assertTrue(sessionId.isNotBlank())
        return sessionId
    }

    private fun waitForTranslationCompletion(sessionId: String) {
        repeat(10) { attempt ->
            val response = executeAuthorizedGraphQLQuery(
                """
                    query {
                        translationSession(sessionId: "$sessionId") {
                            status
                            completedAt
                        }
                    }
                """.trimIndent(),
            )
                .expectStatus().isOk
                .expectBody(String::class.java)
                .returnResult()

            val json = parseJson(response.responseBody)
            val status = json.path("data").path("translationSession").path("status").asText()
            if (status == "COMPLETED") {
                return
            }
            Thread.sleep(200L * (attempt + 1))
        }
        throw IllegalStateException("Translation session did not complete in time")
    }

    private fun parseJson(body: String?): JsonNode {
        assertNotNull(body)
        return objectMapper.readTree(body)
    }

    private fun executeAuthorizedGraphQLQuery(
        query: String,
        token: String = adminToken,
        variables: Map<String, Any?>? = null,
    ): WebTestClient.ResponseSpec {
        val payload = mutableMapOf<String, Any?>("query" to query)
        if (variables != null) {
            payload["variables"] = variables
        }

        return webTestClient.post()
            .uri("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(payload)
            .exchange()
    }
}
