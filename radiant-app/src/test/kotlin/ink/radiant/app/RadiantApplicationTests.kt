package ink.radiant.app

import ink.radiant.RadiantApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [RadiantApplication::class])
class RadiantApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun multiModuleIntegrationTest() {
    }
}
