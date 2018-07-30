package com.acky.rio.plugin.mvp.action;

import com.acky.rio.plugin.mvp.dialog.MVPCreateDialog;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.apache.http.util.TextUtils;

public class MVPCreateAction extends AnAction {

  @Override
  public void actionPerformed(AnActionEvent e) {
    showDialog(e);
  }

  /**
   * 显示dialog
   *
   * @param e
   */
  private void showDialog(AnActionEvent e) {
    MVPCreateDialog myDialog = new MVPCreateDialog(e, new MVPCreateDialog.DialogCallBack() {
      @Override
      public void ok(AnActionEvent e, String className, boolean isActivity) {
        new CreateFile(e, className, isActivity).execute();
      }
    });
    myDialog.setVisible(true);
  }

  @Override
  public void update(AnActionEvent e) {
    IdeView ideView = e.getRequiredData(LangDataKeys.IDE_VIEW);
    if (ideView != null && ideView.getOrChooseDirectory() != null) {
      PsiDirectory directory = ideView.getOrChooseDirectory();
      if (directory.getVirtualFile().getPath().contains("src/main/java")) {
        e.getPresentation().setEnabledAndVisible(true);
      } else {
        e.getPresentation().setEnabledAndVisible(false);
      }
    } else {
      e.getPresentation().setEnabledAndVisible(false);
    }
  }

  private class CreateFile extends WriteCommandAction.Simple {
    private final Project project;
    private final String packageName;
    private final String className;
    private final boolean isActivity;
    //private final boolean hasEntity;
    private final PsiElementFactory factory;
    private final JavaDirectoryService directoryService;
    private final PsiDirectory directory;
    private final GlobalSearchScope searchScope;
    private final PsiShortNamesCache psiShortNamesCache;

    public CreateFile(AnActionEvent e, String className, boolean isActivity) {
      super(e.getProject());
      this.packageName = className.toLowerCase();
      this.className = className;
      this.isActivity = isActivity;
      //this.hasEntity = hasEntity;
      this.project = e.getProject();
      factory = JavaPsiFacade.getElementFactory(project);
      directoryService = JavaDirectoryService.getInstance();
      IdeView ideView = e.getRequiredData(LangDataKeys.IDE_VIEW);
      directory = ideView.getOrChooseDirectory();
      searchScope = GlobalSearchScope.allScope(project);
      psiShortNamesCache = PsiShortNamesCache.getInstance(project);
    }

    @Override
    protected void run() {
      PsiDirectory pDirectory;

      /**
       * 创建presenter目录和contract目录
       */
      pDirectory = directory.findSubdirectory(packageName);
      if (pDirectory == null) {
        pDirectory = directory.createSubdirectory(packageName);
      }

      /**
       * 判断该类是否已经建立过了
       */
      if (pDirectory.findFile(className + "Contract.java") != null
          || pDirectory.findFile(className + "Contract.java") != null
          || pDirectory.findFile(className + "Activity.java") != null
          || pDirectory.findFile(className + "Fragment.java") != null) {
        Messages.showErrorDialog("生成失败\n" + className + " 已经有文件存在", "File already exists");
        return;
      }

      /**
       * 创建Import
       */
      PsiImportStatement importCommonPresenter = getPsiImportStatement("CommonPresenter");
      PsiImportStatement importNonNull =
          getPsiImportStatement("NonNull", "android.support.annotation");

      /**
       * 创建文件
       */
      if (isActivity) {
        PsiClass contract;
        PsiClass activity;
        PsiClass presenter;
        //if (hasEntity) {
          contract = directoryService.createClass(pDirectory, className, "ActivityContract");
          activity = directoryService.createClass(pDirectory, className, "Activity");
          presenter = directoryService.createClass(pDirectory, className, "ActivityPresenter");
        //} else {
        //  contract =
        //      directoryService.createClass(pDirectory, className, "ActivityContractNoEntity");
        //  activity = directoryService.createClass(pDirectory, className, "ActivityNoEntity");
        //  presenter =
        //      directoryService.createClass(pDirectory, className, "ActivityPresenterNoEntity");
        //}
        ((PsiJavaFile) contract.getContainingFile()).getImportList()
                                                    .add(getPsiImportStatement("BaseMVPActivity"));
        ((PsiJavaFile) contract.getContainingFile()).getImportList().add(importCommonPresenter);

        ((PsiJavaFile) activity.getContainingFile()).getImportList().add(importNonNull);
      } else {
        PsiClass contract;
        PsiClass faragment;
        PsiClass presenter;
        //if (hasEntity) {
          contract = directoryService.createClass(pDirectory, className, "FragmentContract");
          faragment = directoryService.createClass(pDirectory, className, "Fragment");
          presenter = directoryService.createClass(pDirectory, className, "FragmentPresenter");
        //} else {
        //  contract =
        //      directoryService.createClass(pDirectory, className, "FragmentContractNoEntity");
        //  faragment = directoryService.createClass(pDirectory, className, "FragmentNoEntity");
        //  presenter =
        //      directoryService.createClass(pDirectory, className, "FragmentPresenterNoEntity");
        //}
        ((PsiJavaFile) contract.getContainingFile()).getImportList()
                                                    .add(getPsiImportStatement("BaseMVPFragment"));
        ((PsiJavaFile) contract.getContainingFile()).getImportList().add(importCommonPresenter);

        ((PsiJavaFile) faragment.getContainingFile()).getImportList().add(importNonNull);
      }
    }

    private PsiClass getPsiClassByName(String className, String packageName) {
      PsiClass[] psiClasses = psiShortNamesCache.getClassesByName(className, searchScope);
      PsiClass psiClass = null;
      if (psiClasses.length != 0) {//if the class already exist.
        if (TextUtils.isEmpty(packageName)) {
          psiClass = psiClasses[0];
        } else {
          for (PsiClass clazz : psiClasses) {
            if (packageName.equals(StringUtil.getPackageName(clazz.getQualifiedName()))) {
              psiClass = clazz;
              break;
            }
          }
        }
      }
      return psiClass;
    }

    private PsiImportStatement getPsiImportStatement(String className) {
      return factory.createImportStatement(getPsiClassByName(className));
    }

    private PsiClass getPsiClassByName(String className) {
      return getPsiClassByName(className, null);
    }

    private PsiImportStatement getPsiImportStatement(String className, String packageName) {
      return factory.createImportStatement(getPsiClassByName(className, packageName));
    }
  }
}