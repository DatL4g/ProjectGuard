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

package com.rubensousa.dependencyguard.plugin.internal.report

import com.rubensousa.dependencyguard.plugin.internal.SuppressionMap

internal class VerificationReportBuilder(
    private val suppressionMap: SuppressionMap,
) {

    fun build(dump: RestrictionDump): VerificationReport {
        val reports = mutableListOf<VerificationModuleReport>()
        dump.modules.forEach { moduleReport ->
            moduleReport.restrictions.forEach { restriction ->
                val fatalMatches = mutableListOf<FatalMatch>()
                val suppressedMatches = mutableListOf<SuppressedMatch>()
                val suppression = suppressionMap.getSuppression(
                    module = moduleReport.module,
                    dependency = restriction.dependency
                )
                if (suppression != null) {
                    suppressedMatches.add(
                        SuppressedMatch(
                            dependency = restriction.dependency,
                            pathToDependency = restriction.pathToDependency ?: restriction.dependency,
                            suppressionReason = suppression.reason
                        )
                    )
                } else {
                    fatalMatches.add(
                        FatalMatch(
                            moduleId = moduleReport.module,
                            dependency = restriction.dependency,
                            pathToDependency = restriction.pathToDependency ?: restriction.dependency,
                            reason = restriction.reason
                        )
                    )
                }
                fatalMatches.sortedBy { it.dependency }
                suppressedMatches.sortedBy { it.dependency }
                reports.add(
                    VerificationModuleReport(
                        module = moduleReport.module,
                        fatal = fatalMatches,
                        suppressed = suppressedMatches
                    )
                )
            }
        }
        return VerificationReport(
            reports.sortedBy { report -> report.module }
        )
    }

}
