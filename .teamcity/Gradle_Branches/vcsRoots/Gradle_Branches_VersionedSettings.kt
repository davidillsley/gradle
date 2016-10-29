package Gradle_Branches.vcsRoots

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.vcs.GitVcsRoot

object Gradle_Branches_VersionedSettings : GitVcsRoot({
    uuid = "ff0bab04-72a7-423c-b969-f99126ee4f27"
    extId = "Gradle_Branches_VersionedSettings"
    name = "Gradle Versioned Settings"
    url = "git@github.com:gradle/gradle.git"
    branch = "versioned-settings"
    authMethod = uploadedKey {
        uploadedKey = "id_rsa_gradlewaregitbot"
    }
})
