package com.lhz

import com.android.build.gradle.api.BaseVariant
import com.meituan.android.walle.ChannelMaker
import com.meituan.android.walle.GradlePlugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

class MyGradlePlugin extends GradlePlugin {

    @Override
    void applyTask(Project project) {
        project.afterEvaluate {
            project.android.applicationVariants.all { BaseVariant variant ->

                println("applyTask start==============")
                def variantName = variant.name.capitalize();

                if (!isV2SignatureSchemeEnabled(variant)) {
                    throw new ProjectConfigurationException("Plugin requires 'APK Signature Scheme v2 Enabled' for ${variant.name}.", null);
                }

                def taskName = "apk${variantName}"
                ChannelMaker channelMaker = project.tasks.create(taskName, ChannelMaker);
                channelMaker.targetProject = project;
                channelMaker.variant = variant;
                channelMaker.setup();

                println("addTasks() build type task ${taskName}")

//                if (variant.hasProperty('assembleProvider')) {
//                    channelMaker.dependsOn variant.assembleProvider.get()
//                } else {
                    channelMaker.dependsOn variant.assemble
//                }


                def buildTypeName = variant.buildType.name
                println("variant.name:${variant.name}, buildTypeName:${buildTypeName}")
                if (variant.name != buildTypeName) {
                    taskName = "apk${buildTypeName.capitalize()}"
                    ChannelMaker apkTask = project.tasks.findByName(taskName)
                    if (apkTask == null) {
                        apkTask = project.tasks.create(taskName, ChannelMaker)
                        apkTask.targetProject = project;
                        apkTask.variant = variant;
                        apkTask.setup();
                    }
                    apkTask.dependsOn(channelMaker)

                    println("addTasks() build type task ${taskName}")
                }


                println("applyTask end==============")
            }
        }
    }
}