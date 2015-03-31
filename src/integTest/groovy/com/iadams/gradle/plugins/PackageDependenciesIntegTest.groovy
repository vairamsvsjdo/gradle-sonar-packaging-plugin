package com.iadams.gradle.plugins

import com.iadams.gradle.plugins.utils.SonarPackagingBaseIntegSpec
import nebula.test.functional.ExecutionResult

/**
 * Created by iwarapter
 */
class PackageDependenciesIntegTest extends SonarPackagingBaseIntegSpec {

    def setup(){
        copyResources('org/sonar/plugins/example/SamplePlugin.java', 'src/main/java/org/sonar/plugins/example/SamplePlugin.java')
        settingsFile << '''rootProject.name="example"'''
    }

    def "dependencies by sonar are not packaged"() {
        setup:
        //forked for dependency resolution.
        fork = true
        remoteDebug = true
        copyResources('build-with-sonar-dep.gradle', 'build.gradle')

        expect:
        runTasksSuccessfully('build')
        dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/commons-lang-2.6.jar")
        !dependencyExists('build/libs/example-1.0.jar', 'META-INF/lib/sonar-plugin-api-4.5.2.jar')
        !dependencyExists('build/libs/example-1.0.jar', 'META-INF/lib/sonar-squid-3.7.jar')
    }

    def "sonar plugins are not packaged"() {
        setup:
        //forked for dependency resolution.
        fork = true
        remoteDebug = true
        copyResources('build-with-sonar-dep.gradle', 'build.gradle')

        expect:
        ExecutionResult result = runTasksSuccessfully('build')
        println result.standardOutput
        println result.standardError
        dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/commons-lang-2.6.jar")
        !dependencyExists('build/libs/example-1.0.jar', "META-INF/lib/sonar-surefire-plugin-2.4.jar")
    }

    def "build without api in compile fails"(){
        setup:
        file('src/main/java/org/sonar/plugins/example/SamplePlugin.java').delete()
        copyResources('build-without-api.gradle', 'build.gradle')

        when:
        ExecutionResult result = runTasksWithFailure('build')

        then:
        result.standardError.contains("org.codehaus.sonar:sonar-plugin-api should be declared in dependencies")
    }

    def "build with api passes"(){
        setup:
        copyResources('build-with-api.gradle', 'build.gradle')
        fork = true

        when:
        ExecutionResult result = runTasksSuccessfully('build')

        then:
        result.success
    }

    def "build with logging deps logs warnings"(){
        setup:
        copyResources('build-with-logging-deps.gradle', 'build.gradle')
        fork = true

        when:
        ExecutionResult result = runTasksSuccessfully('build')

        then:
        result.standardOutput.contains("The following dependencies should be defined with scope 'provided': [log4j]")
        result.standardOutput.contains("The following dependencies should be defined with scope 'provided': [gwt-user]")
    }
}