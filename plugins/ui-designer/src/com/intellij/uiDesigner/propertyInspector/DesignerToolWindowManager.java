// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.uiDesigner.propertyInspector;

import com.intellij.designer.DesignerEditorPanelFacade;
import com.intellij.designer.LightToolWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.uiDesigner.AbstractToolWindowManager;
import com.intellij.uiDesigner.UIDesignerBundle;
import com.intellij.uiDesigner.designSurface.GuiEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexander Lobas
 */
public class DesignerToolWindowManager extends AbstractToolWindowManager implements Disposable {
  private final DesignerToolWindow myToolWindowPanel;

  public DesignerToolWindowManager(@NotNull Project project) {
    super(project);

    myToolWindowPanel = new DesignerToolWindow(project);
    Disposer.register(this, () -> myToolWindowPanel.dispose());
  }

  public static DesignerToolWindow getInstance(@NotNull GuiEditor designer) {
    DesignerToolWindowManager manager = getInstance(designer.getProject());
    if (manager.isEditorMode()) {
      return (DesignerToolWindow)manager.getContent(designer);
    }
    return manager.myToolWindowPanel;
  }

  public static DesignerToolWindowManager getInstance(@NotNull Project project) {
    return project.getComponent(DesignerToolWindowManager.class);
  }

  @Nullable
  public GuiEditor getActiveFormEditor() {
    return (GuiEditor)getActiveDesigner();
  }

  @Override
  protected void initToolWindow() {
    myToolWindow = ToolWindowManager.getInstance(myProject).registerToolWindow(UIDesignerBundle.message("toolwindow.ui.designer.name"),
                                                                               false, getAnchor(), myProject, true);
    myToolWindow.setIcon(AllIcons.Toolwindows.ToolWindowUIDesigner);

    if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
      myToolWindow.getComponent().putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true");
    }

    initGearActions();

    ContentManager contentManager = myToolWindow.getContentManager();
    Content content = contentManager.getFactory()
      .createContent(myToolWindowPanel.getToolWindowPanel(), UIDesignerBundle.message("toolwindow.ui.designer.title"), false);
    content.setCloseable(false);
    content.setPreferredFocusableComponent(myToolWindowPanel.getComponentTree());
    contentManager.addContent(content);
    contentManager.setSelectedContent(content, true);
    myToolWindow.setAvailable(false, null);
  }

  @Override
  protected void updateToolWindow(@Nullable DesignerEditorPanelFacade designer) {
    myToolWindowPanel.update((GuiEditor)designer);

    if (designer == null) {
      myToolWindow.setAvailable(false, null);
    }
    else {
      myToolWindow.setAvailable(true, null);
      myToolWindow.show(null);
    }
  }

  @Override
  protected ToolWindowAnchor getAnchor() {
    return ToolWindowAnchor.LEFT;
  }

  @Override
  protected LightToolWindow createContent(@NotNull DesignerEditorPanelFacade designer) {
    DesignerToolWindow toolWindowContent = new DesignerToolWindow(myProject);
    toolWindowContent.update((GuiEditor)designer);

    return createContent(designer,
                         toolWindowContent,
                         UIDesignerBundle.message("toolwindow.ui.designer.title"),
                         AllIcons.Toolwindows.ToolWindowUIDesigner,
                         toolWindowContent.getToolWindowPanel(),
                         toolWindowContent.getComponentTree(),
                         320,
                         null);
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "UIDesignerToolWindowManager";
  }
}