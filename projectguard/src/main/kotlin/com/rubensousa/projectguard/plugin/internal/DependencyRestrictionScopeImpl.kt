/*
 * Copyright 2026 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.projectguard.plugin.internal

import com.rubensousa.projectguard.plugin.DependencyRestrictionScope
import com.rubensousa.projectguard.plugin.RestrictDependencyRule
import org.gradle.api.internal.catalog.DelegatingProjectDependency

internal class DependencyRestrictionScopeImpl : DependencyRestrictionScope {

    private val allowed = mutableListOf<ModuleAllowSpec>()
    private var restrictionReason = UNSPECIFIED_REASON

    override fun reason(reason: String) {
        restrictionReason = reason
    }

    override fun allow(vararg modulePath: String) {
        allowed.addAll(modulePath.map { path ->
            ModuleAllowSpec(modulePath = path)
        })
    }

    override fun allow(vararg moduleDelegation: DelegatingProjectDependency) {
        allow(modulePath = moduleDelegation.map { module -> module.path }.toTypedArray())
    }

    override fun applyRule(rule: RestrictDependencyRule) {
        allowed.addAll(rule.getSpecs())
    }

    fun getAllowedModules() = allowed.toList()

    fun getReason() = restrictionReason


}
