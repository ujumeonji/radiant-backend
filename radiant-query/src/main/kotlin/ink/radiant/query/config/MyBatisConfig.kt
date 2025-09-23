package ink.radiant.query.config

import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Configuration

@Configuration
@MapperScan("ink.radiant.query.mapper")
class MyBatisConfig
