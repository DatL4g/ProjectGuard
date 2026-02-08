package com.rubensousa.dependencyguard.plugin.internal

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency

internal class DependencyGraphBuilder {

    private val supportedConfigurations = mutableSetOf(
        "compileClasspath",
        "testCompileClasspath",
        "testFixturesCompileClasspath",
    )
    private val androidConfigurationPatterns = mutableSetOf(
        "androidTestUtil", // To exclude test orchestrator in some modules
        "AndroidTestCompileClasspath" // Tests would include
    )

    fun buildFrom(project: Project): List<DependencyGraph> {
        return project.configurations
            .filter { config -> isConfigurationSupported(config) }
            .map { config ->
                val graph = DependencyGraph(
                    configurationId = config.name,
                )
                val moduleId = project.path
                /**
                 * Until https://github.com/rubensousa/DependencyGuard/issues/3 is resolved,
                 * exclude transitive dependency traversals for test configurations
                 */
                config.incoming.dependencies
                    .forEach { dependency ->
                        when(dependency) {
                            is ProjectDependency -> {
                                if (dependency.path != moduleId) {
                                    graph.addDependency(moduleId, dependency.path)
                                }
                            }
                            is ExternalModuleDependency -> {
                                graph.addDependency(moduleId, "${dependency.group}:${dependency.name}")
                            }
                        }
                    }
                graph
            }
            .filter { graph -> graph.nodes.isNotEmpty() }
    }

    private fun isConfigurationSupported(configuration: Configuration): Boolean {
        if (!configuration.isCanBeResolved) {
            return false
        }
        val name = configuration.name
        if (supportedConfigurations.contains(name)) {
            return true
        }
        return androidConfigurationPatterns.any { pattern ->
            name.contains(pattern)
        }
    }

}
