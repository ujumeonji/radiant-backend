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
    fun `translateToKorean mutation returns translation result for english text`() {
        val mutation = """
            mutation TranslateEnglish {
                translateToKorean(
                    input: {
                        sourceText: "Hello world! This is a test translation."
                    }
                ) {
                    __typename
                    ... on TranslationResult {
                        sessionId
                        translatedText
                        sourceLanguage {
                            code
                            name
                            confidence
                        }
                        sentencePairs {
                            order
                            original
                            translated
                        }
                        metadata {
                            processingTimeMs
                            tokenCount
                            chunkCount
                        }
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
        assertEquals("TranslationResult", payload.path("__typename").asText())
        val sessionId = payload.path("sessionId").asText()
        assertTrue(sessionId.isNotBlank())
        val translatedText = payload.path("translatedText").asText()
        assertTrue(translatedText.isNotBlank())
        assertTrue(translatedText.any { it.code in HANGUL_RANGE })
        val sentencePairs = payload.path("sentencePairs")
        assertEquals("Hello world! This is a test translation.", sentencePairs[0].path("original").asText())
        assertTrue(sentencePairs[0].path("translated").asText().any { it.code in HANGUL_RANGE })
    }

    @Test
    fun `translateToKorean mutation rejects text exceeding 20k characters`() {
        val longText = "a".repeat(20_001)
        val mutation = """
            mutation TranslateTooLong(${ '$' }input: TranslationInput!) {
                translateToKorean(input: ${ '$' }input) {
                    __typename
                    ... on TranslationResult {
                        sessionId
                    }
                    ... on TranslationError {
                        type
                        message
                        code
                    }
                }
            }
        """.trimIndent()

        val variables = mapOf(
            "input" to mapOf("sourceText" to longText),
        )

        executeAuthorizedGraphQLQuery(mutation, variables = variables)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.translateToKorean.__typename").isEqualTo("TranslationError")
            .jsonPath("$.data.translateToKorean.type").isEqualTo("TEXT_TOO_LONG")
            .jsonPath("$.data.translateToKorean.code").isEqualTo("TRANSLATION_TEXT_TOO_LONG")
    }

    @Test
    fun `translationSession query returns session details after translation`() {
        val mutation = """
            mutation StartTranslation {
                translateToKorean(
                    input: {
                        sourceText: "Hello world! This is a test translation."
                    }
                ) {
                    ... on TranslationResult {
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
    fun `translateToKorean mutation requires admin authorization`() {
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

    companion object {
        private val HANGUL_RANGE = 0xAC00..0xD7A3
    }
}
