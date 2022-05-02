package br.com.lume.security.audit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.el.MethodExpression;
import javax.faces.component.ActionSource2;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.BehaviorBase;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.BehaviorListener;
import javax.faces.event.MethodExpressionActionListener;

public class LifeCycleListenerUtils {

    public static void putParamOnContext(FacesContext context, String name, Object param) {
        context.getExternalContext().getRequestMap().put(name, param);
    }

    public static <T> T getParamOnContext(FacesContext context, String name, Class<T> clazz) {
        Object obj = context.getExternalContext().getRequestMap().get(name);
        return clazz.cast(obj);
    }

    public static List<String> getMethodsCallFromPhaseEvent(FacesContext context) {
        List<String> methodExpressions = new ArrayList<>();
        if (!(context.isPostback() && context.getPartialViewContext().isAjaxRequest())) {
            return methodExpressions; // Not an ajax postback.
        }

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String sourceClientId = params.get("javax.faces.source");
        String behaviorEvent = params.get("javax.faces.behavior.event");

        UIComponent source = context.getViewRoot().findComponent(sourceClientId);
        if (source instanceof ClientBehaviorHolder && behaviorEvent != null) {
            for (ClientBehavior behavior : ((ClientBehaviorHolder) source).getClientBehaviors().get(behaviorEvent)) {
                List<BehaviorListener> listeners = LifeCycleListenerUtils.getField(BehaviorBase.class, List.class, behavior);

                if (listeners != null) {
                    for (BehaviorListener listener : listeners) {
                        MethodExpression methodExpression = LifeCycleListenerUtils.getField(listener.getClass(), MethodExpression.class, listener);

                        if (methodExpression != null) {
                            methodExpressions.add(methodExpression.getExpressionString());
                        }
                    }
                }
            }
        }

        if (source instanceof ActionSource2) {
            MethodExpression methodExpression = ((ActionSource2) source).getActionExpression();

            if (methodExpression != null) {
                methodExpressions.add(methodExpression.getExpressionString());
            }
        }

        if (source instanceof UICommand && (methodExpressions == null || methodExpressions.isEmpty())) {
            UICommand component = (UICommand) source;

            if (component.getActionExpression() != null)
                methodExpressions.add(component.getActionExpression().getExpressionString());
            else if (component.getActionListeners().length > 0) {
                methodExpressions.add(LifeCycleListenerUtils.getActionListener((MethodExpressionActionListener) component.getActionListeners()[0]).getExpressionString());
            }
        }

        return methodExpressions; // Do your thing with it.
    }

    public static String getCallerCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null, lastRowLume = null;
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(LifeCycleListenerUtils.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                if (callerClassName == null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName()) && ste.getClassName().startsWith("br.com.lume") && !ste.getClassName().contains("LumeManagedBean")) {
                    return ste.getClassName();
                } else if (ste.getClassName().startsWith("br.com.lume")) {
                    lastRowLume = ste.getClassName();
                }
            }
        }
        return lastRowLume;
    }

    public static UICommand findInvokedCommandComponent(FacesContext context) {
        UIViewRoot view = context.getViewRoot();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        if (context.getPartialViewContext().isAjaxRequest()) {
            UIComponent component = view.findComponent(params.get("javax.faces.source"));
            if (component instanceof UICommand) {
                return (UICommand) component;
            }
        } else {
            for (String clientId : params.keySet()) {
                UIComponent component = view.findComponent(clientId);

                if (component instanceof UICommand) {
                    return (UICommand) component;
                }
            }
        }

        return null;
    }

    public static MethodExpression getActionListener(MethodExpressionActionListener listener) {
        MethodExpression expression = null;
        Field field;

        try {
            field = listener.getClass().getDeclaredField("methodExpressionZeroArg");
            field.setAccessible(true);
            expression = (MethodExpression) field.get(listener);

            if (expression == null) {
                field = listener.getClass().getDeclaredField("methodExpressionOneArg");
                field.setAccessible(true);
                expression = (MethodExpression) field.get(listener);
            }
        } catch (Exception e) {

        }

        return expression;
    }

    @SuppressWarnings("unchecked")
    public static <C, F> F getField(Class<? extends C> classType, Class<F> fieldType, C instance) {
        try {
            for (Field field : classType.getDeclaredFields()) {
                if (field.getType().isAssignableFrom(fieldType)) {
                    field.setAccessible(true);
                    return (F) field.get(instance);
                }
            }
        } catch (Exception e) {
            // Handle?
        }

        return null;
    }

}
