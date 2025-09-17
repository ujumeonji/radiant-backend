package ink.radiant.fixture

import ink.radiant.query.entity.PostEntity
import java.util.UUID

object PostEntityFixture {

    fun createPostEntity(
        id: String = UUID.randomUUID().toString(),
        title: String = "테스트 포스트",
        body: String = "테스트 포스트 내용",
        translatedTitle: String = "Test Post",
        originalSentences: String = "첫 번째 문장\n두 번째 문장",
        translatedSentences: String = "First sentence\nSecond sentence",
        likes: Int = 10,
        commentsCount: Int = 5,
        thumbnailUrl: String = "https://example.com/thumb.jpg",
        authorId: String = "test-author",
    ): PostEntity {
        return PostEntity(
            id = id,
            title = title,
            body = body,
            translatedTitle = translatedTitle,
            originalSentences = originalSentences,
            translatedSentences = translatedSentences,
            likes = likes,
            commentsCount = commentsCount,
            thumbnailUrl = thumbnailUrl,
            authorId = authorId,
        )
    }

    fun createPostEntityList(): List<PostEntity> {
        return listOf(
            createPostEntity(
                id = "post-1",
                title = "첫 번째 포스트",
                body = "첫 번째 포스트의 내용입니다.",
                translatedTitle = "First Post",
                originalSentences = "첫 번째 문장\n두 번째 문장",
                translatedSentences = "First sentence\nSecond sentence",
                likes = 10,
                commentsCount = 5,
                thumbnailUrl = "https://example.com/thumb1.jpg",
                authorId = "author-1",
            ),
            createPostEntity(
                id = "post-2",
                title = "두 번째 포스트",
                body = "두 번째 포스트의 내용입니다.",
                translatedTitle = "Second Post",
                originalSentences = "안녕하세요\n반갑습니다",
                translatedSentences = "Hello\nNice to meet you",
                likes = 20,
                commentsCount = 8,
                thumbnailUrl = "https://example.com/thumb2.jpg",
                authorId = "author-2",
            ),
            createPostEntity(
                id = "post-3",
                title = "세 번째 포스트",
                body = "세 번째 포스트의 내용입니다.",
                translatedTitle = "Third Post",
                originalSentences = "좋은 하루\n되세요",
                translatedSentences = "Have a good day\nThank you",
                likes = 15,
                commentsCount = 3,
                thumbnailUrl = "https://example.com/thumb3.jpg",
                authorId = "author-1",
            ),
        )
    }
}
