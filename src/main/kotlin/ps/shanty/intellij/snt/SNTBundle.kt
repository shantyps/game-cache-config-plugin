package ps.shanty.intellij.snt

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

class SNTBundle : DynamicBundle(BUNDLE) {

    companion object {
        val INSTANCE = SNTBundle()
        private const val BUNDLE = "messages.SNTBundle"

        fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
            return INSTANCE.getMessage(key, *params)
        }

        fun messagePointer(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): Supplier<String> {
            return INSTANCE.getLazyMessage(key, *params)
        }
    }
}