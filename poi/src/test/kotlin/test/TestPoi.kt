package test

import com.github.ixtf.poi.Jpoi
import com.github.ixtf.poi.kotlinx.write
import kotlin.io.path.Path
import org.junit.jupiter.api.Test

class TestPoi {
  @Test
  fun testI() {
    val wb = Jpoi.wb()
    Path("").write(wb)
  }
}
