package ink.radiant.infrastructure.config

import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Configuration

@Configuration
@MapperScan("ink.radiant.infrastructure.mapper")
class MyBatisConfig
