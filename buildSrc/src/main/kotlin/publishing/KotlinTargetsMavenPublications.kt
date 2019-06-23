/*
 * Copyright 2019 Louis Cognault Ayeva Derman. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("PackageDirectoryMismatch")

import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.common
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.js
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.native
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import java.util.Locale

fun KotlinTarget.configureMavenPublication() {
    val suffix = when (platformType) {
        common -> "-metadata"
        jvm -> ""
        js -> "-js"
        androidJvm -> ""
        native -> "-${name.toLowerCase(Locale.ROOT)}"
    }
    mavenPublication {
        val prefix = if (project.isFunPack) "splitties-fun-pack" else "splitties"
        artifactId = "$prefix-${project.name}$suffix"
        if (platformType == androidJvm) {
            val capitalizedPublicationName = "${name.first().toTitleCase()}${name.substring(1)}"
            project.tasks.getByName("generateMetadataFileFor${capitalizedPublicationName}Publication")
                .apply { enabled = false }
        }
    }
    if (platformType == androidJvm) {
        (this as KotlinAndroidTarget).publishLibraryVariants("debug", "release")
        this.publishLibraryVariantsGroupedByFlavor = true
    }
}