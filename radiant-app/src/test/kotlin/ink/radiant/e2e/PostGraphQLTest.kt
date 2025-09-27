package ink.radiant.e2e

import ink.radiant.base.BaseGraphQLTest
import ink.radiant.core.domain.entity.PostEntity
import ink.radiant.core.domain.entity.PostParticipantEntity
import ink.radiant.core.domain.entity.ProfessionalField
import ink.radiant.fixture.AccountEntityFixture
import ink.radiant.fixture.PostEntityFixture
import ink.radiant.infrastructure.repository.AccountRepository
import ink.radiant.infrastructure.repository.PostParticipantRepository
import ink.radiant.infrastructure.repository.PostRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PostGraphQLTest : BaseGraphQLTest() {

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var postParticipantRepository: PostParticipantRepository

    @BeforeEach
    @Transactional
    fun setUp() {
        postParticipantRepository.deleteAll()
        postRepository.deleteAll()
        accountRepository.deleteAll()
        setupTestData()
    }

    private fun setupTestData() {
        val authorIds = createAuthors()
        val savedPosts = postRepository.saveAll(createPosts(authorIds))
        setupParticipants(savedPosts)
    }

    private fun createAuthors(): List<String> {
        val authorOne = AccountEntityFixture.createAccount(
            email = "author1@example.com",
            name = "author_one",
            providerId = "author-one",
            displayName = "Author One",
            avatarUrl = "https://example.com/avatar-author1.jpg",
        )
        authorOne.profile!!.apply {
            bio = "백엔드 번역가"
            location = "Seoul"
            websiteUrl = "https://author.one"
            postsCount = 20
            viewsCount = 840L
            followersCount = 112
            followingCount = 33
            professionalFields.add(ProfessionalField.BACKEND)
        }
        val savedAuthorOne = accountRepository.save(authorOne)

        val authorTwo = AccountEntityFixture.createAccount(
            email = "author2@example.com",
            name = "author_two",
            providerId = "author-two",
            displayName = "Author Two",
            avatarUrl = "https://example.com/avatar-author2.jpg",
        )
        authorTwo.profile!!.apply {
            bio = "프론트엔드 전문 번역가"
            location = "Busan"
            websiteUrl = "https://author.two"
            postsCount = 15
            viewsCount = 560L
            followersCount = 76
            followingCount = 29
            professionalFields.addAll(listOf(ProfessionalField.FRONTEND, ProfessionalField.AI_ML))
        }
        val savedAuthorTwo = accountRepository.save(authorTwo)

        return listOf(savedAuthorOne.id, savedAuthorTwo.id)
    }

    private fun createPosts(authorIds: List<String>): List<PostEntity> {
        val authorOneId = authorIds[0]
        val authorTwoId = authorIds[1]

        return listOf(
            PostEntityFixture.createPostEntity(
                id = "00000000-0000-0000-0000-000000000001",
                title = "첫 번째 포스트",
                body = "첫 번째 포스트의 내용입니다.",
                translatedTitle = "First Post",
                originalSentences = "첫 번째 문장\n두 번째 문장",
                translatedSentences = "First sentence\nSecond sentence",
                likes = 10,
                commentsCount = 5,
                thumbnailUrl = "https://example.com/thumb1.jpg",
                authorId = authorOneId,
            ),
            PostEntityFixture.createPostEntity(
                id = "00000000-0000-0000-0000-000000000002",
                title = "두 번째 포스트",
                body = "두 번째 포스트의 내용입니다.",
                translatedTitle = "Second Post",
                originalSentences = "안녕하세요\n반갑습니다",
                translatedSentences = "Hello\nNice to meet you",
                likes = 20,
                commentsCount = 8,
                thumbnailUrl = "https://example.com/thumb2.jpg",
                authorId = authorTwoId,
            ),
            PostEntityFixture.createPostEntity(
                id = "00000000-0000-0000-0000-000000000003",
                title = "세 번째 포스트",
                body = "세 번째 포스트의 내용입니다.",
                translatedTitle = "Third Post",
                originalSentences = "좋은 하루\n되세요",
                translatedSentences = "Have a good day\nThank you",
                likes = 15,
                commentsCount = 3,
                thumbnailUrl = "https://example.com/thumb3.jpg",
                authorId = authorOneId,
            ),
        )
    }

    private fun setupParticipants(savedPosts: List<PostEntity>) {
        val thirdPost = savedPosts.first { it.id == "00000000-0000-0000-0000-000000000003" }
        val firstPost = savedPosts.first { it.id == "00000000-0000-0000-0000-000000000001" }

        val translatorOne = AccountEntityFixture.createAccount(
            email = "translator1@example.com",
            name = "translator_one",
            providerId = "translator-one",
            displayName = "Translator One",
            avatarUrl = "https://example.com/avatar-translator1.jpg",
        )
        translatorOne.profile!!.apply {
            bio = "전문 번역가"
            location = "Seoul"
            websiteUrl = "https://translator.one"
            postsCount = 12
            viewsCount = 420L
            followersCount = 58
            followingCount = 15
            professionalFields.add(ProfessionalField.BACKEND)
        }
        val savedTranslatorOne = accountRepository.save(translatorOne)

        val translatorTwo = AccountEntityFixture.createAccount(
            email = "translator2@example.com",
            name = "translator_two",
            providerId = "translator-two",
            displayName = "Translator Two",
            avatarUrl = "https://example.com/avatar-translator2.jpg",
        )
        translatorTwo.profile!!.apply {
            bio = "AI 번역 전문가"
            location = "Busan"
            websiteUrl = "https://translator.two"
            postsCount = 8
            viewsCount = 275L
            followersCount = 34
            followingCount = 22
            professionalFields.addAll(listOf(ProfessionalField.AI_ML, ProfessionalField.FRONTEND))
        }
        val savedTranslatorTwo = accountRepository.save(translatorTwo)

        val translatorThree = AccountEntityFixture.createAccount(
            email = "translator3@example.com",
            name = "translator_three",
            providerId = "translator-three",
            displayName = "Translator Three",
            avatarUrl = "https://example.com/avatar-translator3.jpg",
        )
        translatorThree.profile!!.apply {
            bio = "커뮤니티 기여자"
            location = "Daegu"
            postsCount = 5
            viewsCount = 150L
            followersCount = 21
            followingCount = 11
            professionalFields.add(ProfessionalField.DEVOPS)
        }
        val savedTranslatorThree = accountRepository.save(translatorThree)

        postParticipantRepository.saveAll(
            listOf(
                PostParticipantEntity(post = thirdPost, profile = savedTranslatorOne.profile!!),
                PostParticipantEntity(post = thirdPost, profile = savedTranslatorTwo.profile!!),
                PostParticipantEntity(post = firstPost, profile = savedTranslatorThree.profile!!),
            ),
        )
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
                        likesCount
                        commentsCount
                        createdAt
                        author {
                            id
                            username
                            name
                        }
                        participants {
                            totalCount
                            edges {
                                node {
                                        id
                                        username
                                        name
                                    }
                                    cursor
                                }
                            }
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
            .jsonPath("$.data.posts.edges").isArray
            .jsonPath("$.data.posts.edges.length()").isEqualTo(2)
            .jsonPath("$.data.posts.edges[0].node.title").isEqualTo("세 번째 포스트")
            .jsonPath("$.data.posts.edges[1].node.title").isEqualTo("두 번째 포스트")
            .jsonPath("$.data.posts.edges[0].node.author.username").isEqualTo("author_one")
            .jsonPath("$.data.posts.edges[0].node.author.name").isEqualTo("Author One")
            .jsonPath("$.data.posts.edges[0].node.participants.totalCount").isEqualTo(2)
            .jsonPath("$.data.posts.edges[0].node.participants.edges[0].node.username").isEqualTo("translator_one")
            .jsonPath("$.data.posts.edges[0].node.participants.edges[1].node.username").isEqualTo("translator_two")
            .jsonPath("$.data.posts.pageInfo.hasNextPage").isEqualTo(true)
            .jsonPath("$.data.posts.pageInfo.hasPreviousPage").isEqualTo(false)
    }

    @Test
    fun `단일 post 쿼리가 정상 동작한다`() {
        val query = """
            {
                post(id: "00000000-0000-0000-0000-000000000001") {
                    ... on Post {
                        id
                        title
                        body
                        originalSentences
                        translatedSentences
                        likesCount
                        commentsCount
                        thumbnailUrl
                        createdAt
                        updatedAt
                        author {
                            id
                            username
                            name
                        }
                        participants {
                            totalCount
                            edges {
                                node {
                                    id
                                    username
                                    name
                                }
                            }
                        }
                    }
                    ... on PostNotFoundError {
                        message
                        code
                        postId
                    }
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.post.id").isEqualTo("00000000-0000-0000-0000-000000000001")
            .jsonPath("$.data.post.title").isEqualTo("첫 번째 포스트")
            .jsonPath("$.data.post.body").isEqualTo("첫 번째 포스트의 내용입니다.")
            .jsonPath("$.data.post.originalSentences[0]").isEqualTo("첫 번째 문장")
            .jsonPath("$.data.post.originalSentences[1]").isEqualTo("두 번째 문장")
            .jsonPath("$.data.post.likesCount").isEqualTo(10)
            .jsonPath("$.data.post.commentsCount").isEqualTo(5)
            .jsonPath("$.data.post.author.username").isEqualTo("author_one")
            .jsonPath("$.data.post.author.name").isEqualTo("Author One")
            .jsonPath("$.data.post.participants.totalCount").isEqualTo(1)
            .jsonPath("$.data.post.participants.edges[0].node.username").isEqualTo("translator_three")
    }

    @Test
    fun `존재하지 않는 post 조회 시 PostNotFoundError를 반환한다`() {
        val query = """
            {
                post(id: "non-existent") {
                    ... on Post {
                        id
                        title
                    }
                    ... on PostNotFoundError {
                        message
                        code
                        postId
                    }
                }
            }
        """.trimIndent()

        executeGraphQLQuery(query)
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data.post.code").isEqualTo("POST_NOT_FOUND")
            .jsonPath("$.data.post.postId").isEqualTo("non-existent")
            .jsonPath("$.data.post.message").isEqualTo("Post with id 'non-existent' not found")
    }

    @Test
    fun `삭제된 post는 조회되지 않는다`() {
        // 소프트 삭제 실행
        val postToDelete = postRepository.findById(
            "00000000-0000-0000-0000-000000000002",
        ).orElseThrow()
        postToDelete.markAsDeleted(deletedBy = null)
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
            .jsonPath("$.data.posts.edges").isArray
            .jsonPath("$.data.posts.edges.length()").isEqualTo(2)
            .jsonPath("$.data.posts.edges[0].node.title").isEqualTo("세 번째 포스트")
            .jsonPath("$.data.posts.edges[1].node.title").isEqualTo("첫 번째 포스트")
    }
}
