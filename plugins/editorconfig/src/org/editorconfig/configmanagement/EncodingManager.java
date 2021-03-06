// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.editorconfig.configmanagement;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingProjectManager;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import org.editorconfig.Utils;
import org.editorconfig.core.EditorConfig.OutPair;
import org.editorconfig.plugincomponents.SettingsProviderComponent;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncodingManager implements FileDocumentManagerListener {
  // Handles the following EditorConfig settings:
  public static final String charsetKey = "charset";

  private final Project myProject;

  public static final Map<String, Charset> encodingMap;

  static {
    Map<String, Charset> map = new HashMap<>();
    map.put("latin1", Charset.forName("ISO-8859-1"));
    map.put("utf-8", CharsetToolkit.UTF8_CHARSET);
    map.put("utf-16be", CharsetToolkit.UTF_16BE_CHARSET);
    map.put("utf-16le", CharsetToolkit.UTF_16LE_CHARSET);
    encodingMap = Collections.unmodifiableMap(map);
  }

  private boolean isApplyingSettings;

  public EncodingManager(Project project) {
    this.myProject = project;
    isApplyingSettings = false;
  }

  @Override
  public void beforeDocumentSaving(@NotNull Document document) {
    final VirtualFile file = FileDocumentManager.getInstance().getFile(document);
    if (!isApplyingSettings) {
      applySettings(file);
    }
  }

  private void applySettings(VirtualFile file) {
    if (file == null) return;
    if (!Utils.isEnabled(CodeStyle.getSettings(myProject))) return;

    // Prevent "setEncoding" calling "saveAll" from causing an endless loop
    isApplyingSettings = true;
    try {
      final List<OutPair> outPairs = SettingsProviderComponent.getInstance().getOutPairs(myProject, file);
      final EncodingProjectManager encodingProjectManager = EncodingProjectManager.getInstance(myProject);
      final String charset = Utils.configValueForKey(outPairs, charsetKey);
      if (!charset.isEmpty()) {
        final Charset newCharset = encodingMap.get(charset);
        if (newCharset != null) {
          if (Comparing.equal(newCharset, file.getCharset())) return;
          encodingProjectManager.setEncoding(file, newCharset);
        } else {
          Utils.invalidConfigMessage(myProject, charset, charsetKey, file.getCanonicalPath());
        }
      }
    } finally {
      isApplyingSettings = false;
    }
  }
}
