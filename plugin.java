package com.hk.intellijplugin;
 
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
 
public class PrintProjectNameAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            String projectName = project.getName();
            System.out.println("Project Name: " + projectName);
        } else {
            System.out.println("No project found.");
        }
    }
}