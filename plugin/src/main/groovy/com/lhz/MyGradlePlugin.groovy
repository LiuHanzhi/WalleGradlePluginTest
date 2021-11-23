package com.lhz

import com.android.build.gradle.api.BaseVariant
import com.meituan.android.walle.ChannelMaker
import com.meituan.android.walle.GradlePlugin
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException

class MyGradlePlugin extends GradlePlugin {

    public static final String DESCRIPTION =  "Make Multi-Channel"
    public static final String GROUP = "Package"

    @Override
    void applyTask(Project project) {
        project.afterEvaluate {
            project.android.applicationVariants.all { BaseVariant vt ->

                println("applyTask start==============")
                def variantName = vt.name.capitalize();

                if (!isV2SignatureSchemeEnabled(vt)) {
                    throw new ProjectConfigurationException("Plugin requires 'APK Signature Scheme v2 Enabled' for ${vt.name}.", null);
                }

                def taskName = "apk${variantName}"
                ChannelMaker channelMaker = project.tasks.create(taskName, ChannelMaker) {
                    description = DESCRIPTION
                    group = GROUP
                    targetProject = project
                    variant = vt
                }

                println("addTasks() build type task ${taskName}")

                if (vt.hasProperty('assembleProvider')) {
                    channelMaker.dependsOn vt.assembleProvider.get()
                } else {
                    channelMaker.dependsOn vt.assemble
                }


                def buildTypeName = vt.buildType.name
                println("variant.name:${vt.name}, buildTypeName:${buildTypeName}")
                if (vt.name != buildTypeName) {
                    taskName = "apk${buildTypeName.capitalize()}"

                    DefaultTask apkTask = project.tasks.findByName(taskName)
                    if (apkTask == null) {
                        apkTask = project.tasks.create(taskName, DefaultTask) {
                            description = DESCRIPTION
                            group = GROUP
                        }
                    }
                    apkTask.dependsOn(channelMaker)

                    println("addTasks() build type task ${taskName}")
                }


                println("applyTask end==============")
            }
        }
    }
}