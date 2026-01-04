package com.github.hirofumi.termback

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager

/**
 * Returns open projects excluding those being disposed.
 *
 * [ProjectManager.getOpenProjects] may include projects mid-disposal.
 *
 * @see <a href="https://github.com/JetBrains/intellij-community/blob/idea/253.29346.138/platform/projectModel-api/src/com/intellij/openapi/project/ProjectManager.java#L70-L74">ProjectManager.getOpenProjects</a>
 */
val ProjectManager.availableProjects: List<Project>
    get() = openProjects.filter { !it.isDisposed }
