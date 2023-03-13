package ps.shanty.intellij.mod

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

class ModBundle : DynamicBundle(BUNDLE) {

    companion object {
        val INSTANCE: ModBundle = ModBundle()
        private const val BUNDLE = "messages.ModBundle"

        fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
            return INSTANCE.getMessage(key, *params)
        }

        fun messagePointer(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): Supplier<String> {
            return INSTANCE.getLazyMessage(key, *params)
        }
    }
}