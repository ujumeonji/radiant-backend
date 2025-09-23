package ink.radiant.query.mapper

import ink.radiant.query.model.TrendingQueryModel
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.OffsetDateTime

@Mapper
interface TrendingQueryMapper {

    fun findTrendingPostsSince(
        @Param("since") since: OffsetDateTime,
        @Param("limit") limit: Int,
    ): List<TrendingQueryModel>

    fun findAllTrendingPosts(@Param("limit") limit: Int): List<TrendingQueryModel>

    fun findByPostIdAndNotDeleted(@Param("postId") postId: String): TrendingQueryModel?
}
