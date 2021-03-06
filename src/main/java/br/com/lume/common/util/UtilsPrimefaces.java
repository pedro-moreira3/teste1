package br.com.lume.common.util;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;

import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inputmask.InputMask;
import org.primefaces.component.inputnumber.InputNumber;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.picklist.PickList;
import org.primefaces.component.radiobutton.RadioButton;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.component.selectonemenu.SelectOneMenu;

public class UtilsPrimefaces {

    /**
     * Disable all the children components
     * 
     * @param uiComponentName
     */
    public static void disableUIComponent(String uiComponentName) {
        UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(uiComponentName);
        if (component != null) {
            disableAll(component.getChildren());
        }
    }

    /**
     * Recursive method to disable the list
     * 
     * @param components
     *            Widget PD list
     */
    private static void disableAll(List<UIComponent> components) {

        for (UIComponent component : components) {
            if (component instanceof InputText) {
                ((InputText) component).setDisabled(true);
            } else if (component instanceof InputNumber) {
                ((InputNumber) component).setDisabled(true);
            } else if (component instanceof PickList) {
                ((PickList) component).setDisabled(true);
            } else if (component instanceof InputTextarea) {
                ((InputTextarea) component).setDisabled(true);

            } else if (component instanceof HtmlInputText) {
                ((HtmlInputText) component).setDisabled(true);

            } else if (component instanceof SelectOneMenu) {
                ((SelectOneMenu) component).setDisabled(true);

            } else if (component instanceof SelectBooleanCheckbox) {
                ((SelectBooleanCheckbox) component).setDisabled(true);
            } else if (component instanceof CommandButton) {
                ((CommandButton) component).setDisabled(true);
            }
            disableAll(component.getChildren());
        }

    }

    /**
     * Read Only all the children components
     * 
     * @param uiComponentName
     */
    public static void readOnlyUIComponent(String uiComponentName) {
        UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(uiComponentName);
        if (component != null) {
            readOnlyAll(component.getChildren(), true);
        }
    }

    /**
     * Read Only all the children components
     * 
     * @param uiComponentName
     * @param readOnly
     *            - Boolean that represents new value for readOnly, default is true.
     */
    public static void readOnlyUIComponent(String uiComponentName, boolean readOnly) {
        UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(uiComponentName);
        if (component != null) {
            readOnlyAll(component.getChildren(), readOnly);
        }
    }

    /**
     * Recursive method to read only the list
     * 
     * @param components
     *            Widget PD list
     */
    private static void readOnlyAll(List<UIComponent> components, boolean readOnly) {

        for (UIComponent component : components) {
            if (component instanceof InputText) {
                ((InputText) component).setReadonly(readOnly);
            } else if (component instanceof Calendar) {
                ((Calendar) component).setDisabled(readOnly);
            } else if (component instanceof PickList) {
                ((PickList) component).setDisabled(readOnly);
            } else if (component instanceof InputNumber) {
                ((InputNumber) component).setReadonly(readOnly);
            } else if (component instanceof InputTextarea) {
                ((InputTextarea) component).setReadonly(readOnly);
            } else if (component instanceof InputMask) {
                ((InputMask) component).setReadonly(readOnly);
            } else if (component instanceof HtmlInputText) {
                ((HtmlInputText) component).setReadonly(readOnly);
            } else if (component instanceof SelectOneMenu) {
                ((SelectOneMenu) component).setDisabled(readOnly);
            } else if (component instanceof SelectBooleanCheckbox) {
                ((SelectBooleanCheckbox) component).setDisabled(readOnly);
            } else if (component instanceof CommandButton) {
                ((CommandButton) component).setDisabled(readOnly);
            } else if (component instanceof UISelectItem) {
                ((UISelectItem) component).setItemDisabled(readOnly);
            }
            readOnlyAll(component.getChildren(), readOnly);
        }

    }

}
