package ink.radiant.query.mapper

import ink.radiant.query.model.PostQueryModel
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import java.time.OffsetDateTime
@Mapper
interface PostQueryMapper {

    fun findAllOrderByCreatedAtDesc(@Param("limit") limit: Int): List<PostQueryModel>

    fun findAllAfterCursor(@Param("cursor") cursor: OffsetDateTime, @Param("limit") limit: Int): List<PostQueryModel>

    fun findByIdAndNotDeleted(@Param("id") id: String): PostQueryModel?

    fun countExistingPosts(): Long

    fun findByIdsAndNotDeleted(@Param("ids") ids: List<String>): List<PostQueryModel>
}
