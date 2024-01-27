@file:Suppress("UNCHECKED_CAST")

package com.lasley.kts_viewer.data

object LocalDatabase {
    private val data = mutableMapOf<String, Any>()

    val values: List<Any>
        get() = data.values.toList()

    fun clearData() {
        data.clear()
    }

    operator fun get(key: String?): Any? {
        return if (key == null) null else data[key]
    }

    /**
     * Registers [content] at key [id]
     *
     * @param duplicateStrategy Used to determine if [content] (in some way) already exists in [data].
     * @return [id] if the add was a success, or the duplicate key as found from [duplicateStrategy].
     * - Note: content won't be added if a duplicate was found
     */
    fun <T : Any> register(
        id: String,
        content: T,
        duplicateStrategy: (input: T, db: T) -> Boolean = { _, _ -> false }
    ): String {
        if (data.containsKey(id)) return id
        val dupValueKey = data
            .filter { (it.value as? T) != null }
            .firstNotNullOfOrNull {
                if (duplicateStrategy(content, it.value as T))
                    it.key else null
            }

        if (dupValueKey != null) return dupValueKey

        data[id] = content
        return id
    }

    /**
     * Returns [content] if it can be registered, or the active content which is registered.
     */
    fun <T> canRegisterOrDefault(
        id: String,
        content: T,
        duplicateStrategy: (input: T, db: T) -> Boolean = { _, _ -> false }
    ): T {
        if (data.containsKey(id)) return data[id] as T
        val dupValue = data
            .filter { (it.value as? T) != null }
            .firstNotNullOfOrNull {
                if (duplicateStrategy(content, it.value as T))
                    (it.value as T) else null
            }
        return dupValue ?: content
    }

    fun remove(id: String) = data.remove(id) != null

    fun update(id: String, newData: Any) {
        data[id] = newData
    }
}