package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.extensions.PackagingExtension
import com.iadams.gradle.plugins.extensions.PackagingOrganizationExtension
import com.iadams.gradle.plugins.tasks.PackagePluginTask
import com.iadams.gradle.plugins.tasks.SonarApiRestartTask
import com.iadams.gradle.plugins.tasks.SonarPluginDeployTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Created by iwarapter
 */
class SonarPackagingPlugin implements Plugin<Project> {

    static final SONAR_PACKAGING_GROUP = 'Sonar Packaging'
    static final SONAR_PACKAGING_EXTENSION = 'sonarpackaging'
    static final SONAR_PACKAGING_ORGANIZATION_EXTENSION = 'organization'
    static final SONAR_PLUGIN_LOCAL_DEPLOY_TASK = 'localDeploy'
    static final SONAR_API_RESTART_TASK = 'restartServer'

    @Override
    void apply(Project project) {

        project.plugins.apply JavaPlugin
        project.extensions.create( SONAR_PACKAGING_EXTENSION, PackagingExtension, project)
        project.extensions.findByName(SONAR_PACKAGING_EXTENSION).extensions.create( SONAR_PACKAGING_ORGANIZATION_EXTENSION, PackagingOrganizationExtension)
        project.configurations.create('provided')
        project.sourceSets.main.compileClasspath += project.configurations.provided
        project.sourceSets.test.compileClasspath += project.configurations.provided
        project.sourceSets.test.runtimeClasspath += project.configurations.provided

        addTasks(project)
    }

    /**
     * Adds the plugin deploy and server restart tasks.
     *
     * @param project
     */
    void addTasks(Project project){
        def extension = project.extensions.findByName(SONAR_PACKAGING_EXTENSION)

        project.task( 'pluginPackaging', type: PackagePluginTask) {
            description = 'Updates the Jar file with the correct dependencies and manifest info.'
            group = SONAR_PACKAGING_GROUP
        }

        project.tasks.findByName('jar').finalizedBy 'pluginPackaging'

        project.task( SONAR_PLUGIN_LOCAL_DEPLOY_TASK, type: SonarPluginDeployTask) {
            description = 'Copies the built plugin to the local server.'
            group = SONAR_PACKAGING_GROUP

            conventionMapping.localServer = { project.file(extension.pluginDir) }
            conventionMapping.pluginJar = { project.file("${project.libsDir}/${project.archivesBaseName}-${project.version}.jar") }
        }

        project.task( SONAR_API_RESTART_TASK, type: SonarApiRestartTask) {
            description = 'Restarts a SonarQube server running in dev mode.'
            group = SONAR_PACKAGING_GROUP

            conventionMapping.serverUrl = { extension.serverUrl }
            conventionMapping.restartApiPath = { extension.restartApiPath }
        }
    }
}
