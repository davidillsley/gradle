package Gradle_Branches_CommitPhase_macOSCommit_macOSCommitJava18

import Gradle_Branches_CommitPhase_macOSCommit_macOSCommitJava18.buildTypes.*
import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.Project

object Project : Project({
    uuid = "d8ddb860-cffa-494c-99ee-85be94dfd162"
    extId = "Gradle_Branches_CommitPhase_macOSCommit_macOSCommitJava18"
    parentId = "Gradle_Branches_CommitPhase_macOSCommit"
    name = "macOS commit - Java 1.8"
    description = "Fast verification on macOS through in-process tests"

    buildType(Gradle_Branches_CommitPhase_macOSCommit_macOSCommitJava18_1macOSCommitJava18)
})
