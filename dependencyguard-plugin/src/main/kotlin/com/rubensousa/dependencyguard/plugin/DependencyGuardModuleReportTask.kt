package com.rubensousa.dependencyguard.plugin

import com.rubensousa.dependencyguard.plugin.internal.DependencyGraph
import com.rubensousa.dependencyguard.plugin.internal.DependencyGuardSpec
import com.rubensousa.dependencyguard.plugin.internal.RestrictionChecker
import com.rubensousa.dependencyguard.plugin.internal.RestrictionMatch
import com.rubensousa.dependencyguard.plugin.internal.RestrictionMatchProcessor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Report should always be generated")
abstract class DependencyGuardModuleReportTask : DefaultTask() {

    @get:Input
    internal abstract val projectPath: Property<String>

    @get:Input
    internal abstract val specProperty: Property<DependencyGuardSpec>

    @get:Input
    internal abstract val dependencies: ListProperty<DependencyGraph>

    @get:OutputFile
    internal abstract val reportFile: RegularFileProperty

    @TaskAction
    fun dependencyGuardReport() {
        val spec = specProperty.get()
        reportFile.get().asFile.delete()
        if (spec.isEmpty()) {
            return
        }
        val currentModulePath = projectPath.get()
        val matches = mutableListOf<RestrictionMatch>()
        val restrictionChecker = RestrictionChecker()
        val processor = RestrictionMatchProcessor()
        dependencies.get().forEach { graph ->
            matches.addAll(
                restrictionChecker.findRestrictions(
                    modulePath = currentModulePath,
                    dependencyGraph = graph,
                    spec = spec
                )
            )
        }
        val processedMatches = processor.process(matches)
        if (processedMatches.isNotEmpty()) {
            reportFile.get().asFile.writeText(Json.encodeToString(processedMatches))
        } else {
            reportFile.get().asFile.delete()
        }
    }
}
