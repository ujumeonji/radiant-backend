package ink.radiant.e2e

import ink.radiant.base.BaseGraphQLTest
import ink.radiant.fixture.PostEntityFixture
import ink.radiant.fixture.TrendingEntityFixture
import ink.radiant.query.repository.PostRepository
import ink.radiant.query.repository.TrendingRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TrendingGraphQLTest : BaseGraphQLTest() {

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var trendingRepository: TrendingRepository

    @BeforeEach
    @Transactional
    fun setUp() {
        postRepository.deleteAll()
        trendingRepository.deleteAll()
        setupTestData()
    }

    private fun setupTestData() {
        val testPosts = PostEntityFixture.createPostEntityList()
        postRepository.saveAll(testPosts)

        val testTrending = TrendingEntityFixture.createTrendingEntityList()
        trendingRepository.saveAll(testTrending)
    }

    @Test
    fun `trendingPosts 쿼리가 기본 동작한다`() {
        val query = """
            {
                trendingPosts {
                    id
                    title
                    body
                    likes
                    commentsCount
                    createdAt
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts").isArray
            .jsonPath("$.data.trendingPosts.length()").isEqualTo(3)
            .jsonPath("$.data.trendingPosts[0].id").isEqualTo("post-2")
            .jsonPath("$.data.trendingPosts[0].title").isEqualTo("두 번째 포스트")
            .jsonPath("$.data.trendingPosts[1].id").isEqualTo("post-3")
            .jsonPath("$.data.trendingPosts[1].title").isEqualTo("세 번째 포스트")
            .jsonPath("$.data.trendingPosts[2].id").isEqualTo("post-1")
            .jsonPath("$.data.trendingPosts[2].title").isEqualTo("첫 번째 포스트")
    }

    @Test
    fun `trendingPosts 쿼리에서 limit 파라미터가 동작한다`() {
        val query = """
            {
                trendingPosts(limit: 2) {
                    id
                    title
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts").isArray
            .jsonPath("$.data.trendingPosts.length()").isEqualTo(2)
            .jsonPath("$.data.trendingPosts[0].id").isEqualTo("post-2")
            .jsonPath("$.data.trendingPosts[1].id").isEqualTo("post-3")
    }

    @Test
    fun `trendingPosts 쿼리에서 limit이 범위를 벗어나면 조정된다`() {
        val query = """
            {
                trendingPosts(limit: 100) {
                    id
                    title
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts").isArray
            .jsonPath("$.data.trendingPosts.length()").isEqualTo(3)
    }

    @Test
    fun `트렌드 데이터가 없으면 빈 배열을 반환한다`() {
        trendingRepository.deleteAll()

        val query = """
            {
                trendingPosts {
                    id
                    title
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts").isArray
            .jsonPath("$.data.trendingPosts.length()").isEqualTo(0)
    }
}
