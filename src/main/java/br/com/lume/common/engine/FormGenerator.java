package br.com.lume.common.engine;

import java.lang.reflect.Field;

import javax.persistence.Id;

import org.apache.log4j.Logger;

public class FormGenerator {

    private Logger log = Logger.getLogger(FormGenerator.class);

    private FormGenerator(
            String packageName) {

//        try {
//            ClassFinder classFinder = new ClassFinder();
//            for (Class<?> clazz: classFinder.getClasses(packageName)) {
//                log.debug(createForm(PerfilUsuario.class));
//            }
//        } catch (ClassNotFoundException e) {
//           log.error("Erro", e);
//        } catch (IOException e) {
//            log.error("Erro", e);
//        }

    }

    private String createForm(Class<?> entity) {

        StringBuilder xhtml = new StringBuilder();

        xhtml.append("<! -- Form for <ENTITY> -->");
        xhtml.append("\n");
        xhtml.append("\n");
        xhtml.append("<ui:composition xmlns=\"http://www.w3.org/1999/xhtml\" \n");
        xhtml.append("                xmlns:h=\"http://java.sun.com/jsf/html\" \n");
        xhtml.append("                xmlns:f=\"http://java.sun.com/jsf/core\" \n");
        xhtml.append("                xmlns:rich=\"http://richfaces.org/rich\" \n");
        xhtml.append("                xmlns:ui=\"http://java.sun.com/jsf/facelets\" \n");
        xhtml.append("                template=\"../templates/default.xhtml\"> \n");
        xhtml.append("\n");
        xhtml.append("\t<f:loadBundle basename=\"br.com.lume.resources.security\" var=\"msg\" /> \n");
        xhtml.append("\n");
        xhtml.append("\t<ui:define name=\"article\"> \n");
        xhtml.append("\n");
        xhtml.append("\t\t<ui:include src=\"../pages/commons/messages.xhtml\"></ui:include> \n");
        xhtml.append("\n");
        for (Field field : entity.getDeclaredFields()) {
            xhtml.append(this.createHideenField(field.getName()));
        }
        xhtml.append("\n");
        xhtml.append("\t\t<rich:panel header=\"Cadastro de <ENTITY>\"> \n");
        xhtml.append("\n");
        for (Field field : entity.getDeclaredFields()) {
            xhtml.append(this.createField(field.getName()));
        }
        xhtml.append(this.createBottons(entity.getSimpleName().toLowerCase()));
        xhtml.append("\t\t</rich:panel> \n");
        xhtml.append("\n");
        xhtml.append(this.createTable(entity));
        xhtml.append("\t</ui:define> \n");
        xhtml.append("</ui:composition> \n");
        xhtml.append("\n");
        xhtml.append("\n");
        xhtml.append("<! -- End form for <ENTITY> -->");

        return xhtml.toString().replaceAll("<ENTITY>", entity.getSimpleName().toLowerCase());
    }

    private String createFieldId(Class<?> entity) {

        String fieldName = null;

        StringBuilder field = new StringBuilder();

        for (Field f : entity.getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                fieldName = f.getName();
                field.append("\n");
                field.append("\t\t\t<h:inputHidden id=\"<FIELD>\" value=\"#{<ENTITY>MB.entity.<FIELD>}\"/>" + "\n");
                break;
            }
        }

        return fieldName != null ? field.toString().replaceAll("<FIELD>", fieldName) : "";
    }

    private String createHideenField(String fieldName) {

        if ("serialVersionUID".equals(fieldName)) {
            return "";
        }

        StringBuilder field = new StringBuilder();

        field.append("\t\t\t<h:inputHidden id=\"<FIELD>\" value=\"#{<ENTITY>MB.entity.<FIELD>}\" /> \n");

        return field.toString().replaceAll("<FIELD>", fieldName);
    }

    private String createField(String fieldName) {

        if ("serialVersionUID".equals(fieldName)) {
            return "";
        }

        StringBuilder field = new StringBuilder();

        field.append("\t\t\t<h:panelGroup styleClass=\"lumeFieldRequired\"> \n");
        field.append("\t\t\t\t<h:outputLabel for=\"<FIELD>\" value=\"Label for <FIELD> : \" /> \n");
        field.append("\t\t\t\t<h:inputText id=\"<FIELD>\" value=\"#{<ENTITY>MB.entity.<FIELD>}\" style=\"width: 200px;\" required=\"true\" requiredMessage=\"#{msg['erro.required']}\" /> \n");
        field.append("\t\t\t\t<h:message for=\"<FIELD>\" styleClass=\"msg\" /> \n");
        field.append("\t\t\t</h:panelGroup> \n");
        field.append("\n");

        return field.toString().replaceAll("<FIELD>", fieldName);
    }

    private String createBottons(String entityName) {

        StringBuilder bottons = new StringBuilder();

        bottons.append("\t\t\t<rich:panel styleClass=\"lumeBotton\"> \n");
        bottons.append("\t\t\t\t<h:commandButton id=\"new\" value=\"Novo\" actionListener=\"#{<ENTITY>MB.actionNew}\"> \n");
        bottons.append("\t\t\t\t\t<f:ajax render=\"@form\" /> \n");
        bottons.append("\t\t\t\t</h:commandButton> \n");
        bottons.append("\t\t\t\t<h:commandButton id=\"persist\" value=\"Salvar\" actionListener=\"#{<ENTITY>MB.actionPersist}\"> \n");
        bottons.append("\t\t\t\t\t<f:ajax execute=\"@form\" render=\"@form\" /> \n");
        bottons.append("\t\t\t\t</h:commandButton> \n");
        bottons.append("\t\t\t\t<h:commandButton id=\"remove\" value=\"Deletar\" actionListener=\"#{<ENTITY>MB.actionRemove}\"> \n");
        bottons.append("\t\t\t\t\t<f:ajax execute=\"@form\" render=\"@form\" /> \n");
        bottons.append("\t\t\t\t</h:commandButton> \n");
        bottons.append("\t\t\t</rich:panel> \n");

        return bottons.toString().replaceAll("<ENTITY>", entityName);
    }

    private String createTable(Class<?> entity) {

        StringBuilder table = new StringBuilder();

        table.append("\t\t<f:ajax render=\"@form\"> \n");
        table.append("\t\t\t<rich:panel header=\"<ENTITY>\" styleClass=\"lumeTable\"> \n");
        table.append("\t\t\t\t<rich:dataTable id=\"table_<ENTITY>\" value=\"#{<ENTITY>MB.entityList}\" var=\"<ENTITY>\" rows=\"10\" > \n");

        for (Field field : entity.getDeclaredFields()) {
            table.append(this.createColunm(entity.getSimpleName().toLowerCase(), field.getName()));
        }

        table.append("\t\t\t\t</rich:dataTable> \n");
        table.append("\t\t\t\t<rich:dataScroller id=\"scroller_<ENTITY>\" for=\"table_<ENTITY>\" /> \n");
        table.append("\t\t\t</rich:panel> \n");
        table.append("\t\t</f:ajax> \n");

        return table.toString();
    }

    private String createColunm(String entityName, String fieldName) {

        if ("serialVersionUID".equals(fieldName)) {
            return "";
        }

        StringBuilder colunm = new StringBuilder();

        colunm.append("\n");
        colunm.append("\t\t\t\t\t<rich:column> \n");
        colunm.append("\t\t\t\t\t\t<f:facet name=\"header\"> \n");
        colunm.append("\t\t\t\t\t\t\t<h:outputText value=\"<FIELD>\" /> \n");
        colunm.append("\t\t\t\t\t\t</f:facet> \n");
        colunm.append("\t\t\t\t\t\t<h:commandLink value=\"#{<ENTITY>.<FIELD>}\"> \n");
        colunm.append("\t\t\t\t\t\t\t<f:setPropertyActionListener value=\"#{<ENTITY>}\" target=\"#{<ENTITY>MB.entity}\" /> \n");
        colunm.append("\t\t\t\t\t\t</h:commandLink> \n");
        colunm.append("\t\t\t\t\t</rich:column> \n");

        return colunm.toString().replaceAll("<FIELD>", fieldName);
    }

    public static void main(String[] args) {
        new FormGenerator("br.com.lume.security.entity");
        System.exit(0);
    }
}
