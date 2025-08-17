import org.junit.Test // ✅ 使用 JUnit 4 的 Test
import org.junit.Assert.assertEquals // ✅ 使用 JUnit 4 的断言

class ExampleUnitTest {
    @Test // ✅ 现在能正确识别
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}