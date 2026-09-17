import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetBuild
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetPublish
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetRestore
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetTest
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2026.2"

project {
    buildType(Build)
    buildType(Package)
}

object Build : BuildType({
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dotnetRestore {
            name = "Restore"
            projects = "MonApp.slnx"
        }
        dotnetBuild {
            name = "Build"
            projects = "MonApp.slnx"
            configuration = "Release"
            args = "--no-restore"
        }
        dotnetTest {
            name = "Test"
            projects = "MonApp.slnx"
            configuration = "Release"
            args = "--no-build"
        }
    }

    triggers {
        vcs {
            branchFilter = "+:*"
        }
    }

    failureConditions {
        executionTimeoutMin = 15
    }
})

object Package : BuildType({
    name = "Package"

    vcs {
        root(DslContext.settingsRoot)
    }

    artifactRules = "out => monapp-%build.number%.zip"

    steps {
        dotnetPublish {
            name = "Publish"
            projects = "src/MonApp.Api/MonApp.Api.csproj"
            configuration = "Release"
            outputDir = "out"
            args = "-p:Version=1.0.%build.number%"
        }
    }

    dependencies {
        snapshot(Build) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})