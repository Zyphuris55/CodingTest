package com.lasley.kts_viewer

import android.content.ContentProviderClient
import android.content.ContextWrapper

object Constants {
    val providerID = "com.lasley.provider"

    fun providerClient(context: ContextWrapper): ContentProviderClient? {
        return context.contentResolver.acquireContentProviderClient(providerID)
    }

    fun providerExists(context: ContextWrapper): Boolean {
        return providerClient(context).use {
            println("Provider status: $it")
            it != null
        }
    }
}
