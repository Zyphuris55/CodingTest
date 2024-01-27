/*
 * Copyright (C) 2016 Miguel Ángel Moreno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.github.nitrico.lastadapter

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * ViewHolder for this layout
 *
 * @param binding ViewHolder binding; contains the ViewBinding of this holder
 */
open class Holder<B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root) {
    internal var created = false

    /**
     * Status which is set after onCreate of the view type has been called.
     *
     * - Type -> [ItemType.onCreate]
     * - Map -> [Type.onCreate]
     */
    val isCreated get() = created
}
