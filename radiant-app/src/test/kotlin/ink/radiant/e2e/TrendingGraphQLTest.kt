package ink.radiant.e2e

import ink.radiant.base.BaseGraphQLTest
import ink.radiant.fixture.PostEntityFixture
import ink.radiant.fixture.TrendingEntityFixture
import ink.radiant.infrastructure.mapper.TrendingQueryMapper
import ink.radiant.infrastructure.repository.PostRepository
import ink.radiant.infrastructure.repository.TrendingRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Commit
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TrendingGraphQLTest : BaseGraphQLTest() {

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var trendingRepository: TrendingRepository

    @Autowired
    private lateinit var trendingQueryMapper: TrendingQueryMapper

    @BeforeEach
    @Transactional
    @Commit
    fun setUp() {
        postRepository.deleteAll()
        trendingRepository.deleteAll()
        setupTestData()
    }

    private fun setupTestData() {
        val testPosts = PostEntityFixture.createPostEntityList()
        postRepository.saveAll(testPosts)
        postRepository.flush()

        val testTrending = TrendingEntityFixture.createTrendingEntityList()
        trendingRepository.saveAll(testTrending)
        trendingRepository.flush()
    }

    @Test
    fun `trendingPosts 쿼리가 기본 동작한다`() {
        val query = """
            {
                trendingPosts {
                    edges {
                        node {
                            id
                            title
                            body
                            likesCount
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
                    totalCount
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts.edges").isArray
            .jsonPath("$.data.trendingPosts.edges.length()").isEqualTo(3)
            .jsonPath("$.data.trendingPosts.edges[0].node.id").isEqualTo("00000000-0000-0000-0000-000000000002")
            .jsonPath("$.data.trendingPosts.edges[0].node.title").isEqualTo("두 번째 포스트")
            .jsonPath("$.data.trendingPosts.edges[1].node.id").isEqualTo("00000000-0000-0000-0000-000000000003")
            .jsonPath("$.data.trendingPosts.edges[1].node.title").isEqualTo("세 번째 포스트")
            .jsonPath("$.data.trendingPosts.edges[2].node.id").isEqualTo("00000000-0000-0000-0000-000000000001")
            .jsonPath("$.data.trendingPosts.edges[2].node.title").isEqualTo("첫 번째 포스트")
    }

    @Test
    fun `trendingPosts 쿼리에서 first 파라미터가 동작한다`() {
        val query = """
            {
                trendingPosts(first: 2) {
                    edges {
                        node {
                            id
                            title
                        }
                    }
                    totalCount
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts.edges").isArray
            .jsonPath("$.data.trendingPosts.edges.length()").isEqualTo(2)
            .jsonPath("$.data.trendingPosts.edges[0].node.id").isEqualTo("00000000-0000-0000-0000-000000000002")
            .jsonPath("$.data.trendingPosts.edges[1].node.id").isEqualTo("00000000-0000-0000-0000-000000000003")
    }

    @Test
    fun `trendingPosts 쿼리에서 first가 큰 값이어도 정상 동작한다`() {
        val query = """
            {
                trendingPosts(first: 100) {
                    edges {
                        node {
                            id
                            title
                        }
                    }
                    totalCount
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts.edges").isArray
            .jsonPath("$.data.trendingPosts.edges.length()").isEqualTo(3)
    }

    @Test
    fun `트렌드 데이터가 없으면 빈 배열을 반환한다`() {
        trendingRepository.deleteAll()

        val query = """
            {
                trendingPosts {
                    edges {
                        node {
                            id
                            title
                        }
                    }
                    totalCount
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.trendingPosts.edges").isArray
            .jsonPath("$.data.trendingPosts.edges.length()").isEqualTo(0)
            .jsonPath("$.data.trendingPosts.totalCount").isEqualTo(0)
    }
}
