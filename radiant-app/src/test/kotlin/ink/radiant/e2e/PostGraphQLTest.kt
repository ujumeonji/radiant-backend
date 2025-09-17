package ink.radiant.e2e

import ink.radiant.base.BaseGraphQLTest
import ink.radiant.fixture.PostEntityFixture
import ink.radiant.query.repository.PostRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PostGraphQLTest : BaseGraphQLTest() {

    @Autowired
    private lateinit var postRepository: PostRepository

    @BeforeEach
    @Transactional
    fun setUp() {
        postRepository.deleteAll()
        setupTestData()
    }

    private fun setupTestData() {
        val testPosts = PostEntityFixture.createPostEntityList()
        postRepository.saveAll(testPosts)
    }

    @Test
    fun `posts 쿼리가 페이지네이션과 함께 정상 동작한다`() {
        val query = """
            {
                posts(first: 2) {
                    edges {
                        node {
                            id
                            title
                            body
                            likes
                            commentsCount
                            createdAt
                        }
                        cursor
                    }
                    pageInfo {
                        hasNextPage
                        hasPreviousPage
                        startCursor
                        endCursor
                    }
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.posts.edges").isArray
            .jsonPath("$.data.posts.edges.length()").isEqualTo(2)
            .jsonPath("$.data.posts.edges[0].node.title").isEqualTo("세 번째 포스트")
            .jsonPath("$.data.posts.edges[1].node.title").isEqualTo("두 번째 포스트")
            .jsonPath("$.data.posts.pageInfo.hasNextPage").isEqualTo(true)
            .jsonPath("$.data.posts.pageInfo.hasPreviousPage").isEqualTo(false)
    }

    @Test
    fun `단일 post 쿼리가 정상 동작한다`() {
        val query = """
            {
                post(id: "post-1") {
                    id
                    title
                    body
                    originalSentences
                    translatedSentences
                    likes
                    commentsCount
                    thumbnailUrl
                    createdAt
                    updatedAt
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.post.id").isEqualTo("post-1")
            .jsonPath("$.data.post.title").isEqualTo("첫 번째 포스트")
            .jsonPath("$.data.post.body").isEqualTo("첫 번째 포스트의 내용입니다.")
            .jsonPath("$.data.post.originalSentences[0]").isEqualTo("첫 번째 문장")
            .jsonPath("$.data.post.originalSentences[1]").isEqualTo("두 번째 문장")
            .jsonPath("$.data.post.likes").isEqualTo(10)
            .jsonPath("$.data.post.commentsCount").isEqualTo(5)
    }

    @Test
    fun `존재하지 않는 post 조회 시 null을 반환한다`() {
        val query = """
            {
                post(id: "non-existent") {
                    id
                    title
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.post").doesNotExist()
    }

    @Test
    fun `삭제된 post는 조회되지 않는다`() {
        // 소프트 삭제 실행
        val postToDelete = postRepository.findById("post-2").orElseThrow()
        postToDelete.softDelete()
        postRepository.save(postToDelete)

        val query = """
            {
                posts(first: 10) {
                    edges {
                        node {
                            id
                            title
                        }
                    }
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.posts.edges").isArray
            .jsonPath("$.data.posts.edges.length()").isEqualTo(2)
            .jsonPath("$.data.posts.edges[0].node.title").isEqualTo("세 번째 포스트")
            .jsonPath("$.data.posts.edges[1].node.title").isEqualTo("첫 번째 포스트")
    }
}
