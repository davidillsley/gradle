package Gradle_Branches

import Gradle_Branches.vcsRoots.*
import Gradle_Branches.vcsRoots.Gradle_Branches_VersionedSettings
import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v10.Project
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.VersionedSettings.*
import jetbrains.buildServer.configs.kotlin.v10.projectFeatures.versionedSettings

object Project : Project({
    uuid = "23127635-7b1f-4b70-892e-764156648a78"
    extId = "Gradle_Branches"
    parentId = "Gradle"
    name = "Branches"
    description = "Master configurations"

    vcsRoot(Gradle_Branches_VersionedSettings)

    features {
        versionedSettings {
            id = "PROJECT_EXT_62"
            mode = VersionedSettings.Mode.ENABLED
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
            rootExtId = Gradle_Branches_VersionedSettings.extId
            showChanges = true
            settingsFormat = VersionedSettings.Format.KOTLIN
        }
    }
})
