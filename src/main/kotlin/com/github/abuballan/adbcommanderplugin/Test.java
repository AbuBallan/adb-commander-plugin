package com.github.abuballan.adbcommanderplugin;

import com.github.abuballan.adbcommanderplugin.action.AdbAction;
import com.github.abuballan.adbcommanderplugin.action.StartQuickAdbCommandDialogAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Function;

public class Test extends AdbAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event, @NotNull Project project) {
        StartQuickAdbCommandDialogAction.ListPopupStepImpl listPopupStepImpl = new StartQuickAdbCommandDialogAction.ListPopupStepImpl();

        Function<ListCellRenderer, ListCellRenderer> function = new Function<ListCellRenderer, ListCellRenderer>() {
            @Override
            public ListCellRenderer apply(ListCellRenderer listCellRenderer) {
                return null;
            }
        };

        ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(
                project,
                listPopupStepImpl,
                new Function<ListCellRenderer, ListCellRenderer>() {
                    @Override
                    public ListCellRenderer apply(ListCellRenderer listCellRenderer) {
                        return null;
                    }
                }
        );


    }
}
