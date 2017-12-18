package com.acky.rio.plugin.mvp.dialog;

import com.intellij.openapi.actionSystem.AnActionEvent;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class MVPCreateDialog extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextField textField1;
  private JRadioButton activityRadioButton;
  private JRadioButton fragmentRadioButton;
  private JCheckBox entityCheckBox;

  private AnActionEvent anActionEvent;
  private DialogCallBack callBack;

  public MVPCreateDialog(AnActionEvent anActionEvent, DialogCallBack callBack) {
    setTitle("MVP Classes");
    this.anActionEvent = anActionEvent;
    this.callBack = callBack;
    setContentPane(contentPane);
    getRootPane().setDefaultButton(buttonOK);
    setModal(true);
    setMinimumSize(new Dimension(200, 100));
    pack();
    setLocationRelativeTo(null);

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(activityRadioButton);
    buttonGroup.add(fragmentRadioButton);

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    });

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    });

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
                                         public void actionPerformed(ActionEvent e) {
                                           onCancel();
                                         }
                                       }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  public static void main(String[] args) {
    MVPCreateDialog dialog = new MVPCreateDialog(null, new DialogCallBack() {
      @Override
      public void ok(AnActionEvent e, String className, boolean isActivity, boolean hasEntity) {

      }
    });
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }

  private void onOK() {
    if (callBack != null) {
      callBack.ok(anActionEvent, textField1.getText().trim(), activityRadioButton.isSelected(),
          entityCheckBox.isSelected());
    }
    dispose();
  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
  }

  public interface DialogCallBack {
    void ok(AnActionEvent e, String className, boolean isActivity, boolean hasEntity);
  }
}
