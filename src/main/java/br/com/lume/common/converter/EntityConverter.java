package br.com.lume.common.converter;

import static net.sf.trugger.element.Elements.element;
import static net.sf.trugger.reflection.ReflectionPredicates.annotatedWith;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;

import org.apache.log4j.Logger;
import org.primefaces.component.picklist.PickList;

@FacesConverter(value = "entityConverter")
public class EntityConverter implements Converter {

    protected static final Logger logger = Logger.getLogger(EntityConverter.class);

    @Override
    public Object getAsObject(final FacesContext ctx, final UIComponent component, final String value) {
        logger.debug("getAsObject - Convertendo texto: " + value);
        if (value != null) {
            return component.getAttributes().get(value);
        }
        return null;
    }

    @Override
    public String getAsString(final FacesContext ctx, final UIComponent component, final Object obj) {
        logger.debug("getAsString - Convertendo objeto: " + String.valueOf(obj));
        if (obj != null && !"".equals(obj)) {
            String id;
            id = this.getId(obj);
            if (id == null) {
                id = "";
            }
            id = id.trim();
            if (component.getClass().equals(PickList.class)) {
                component.getAttributes().put(id, obj);
            } else {
                component.getAttributes().put(id, this.getClazz(ctx, component).cast(obj));
            }
            return id;
        }
        return null;
    }

    private Class<?> getClazz(final FacesContext facesContext, final UIComponent component) {
        logger.debug("getClazz - Resolvendo classe do componente: " + component);
        final Class<?> resultado = component.getValueExpression("value").getType(facesContext.getELContext());
        logger.debug("getClazz - Classe do componente " + component + ": " + (resultado != null ? resultado.getName() : "?"));
        return resultado;
    }

    public String getId(final Object obj) {
        logger.debug("getId - Recuperando id do objeto Entity: " + String.valueOf(obj));
        try {
            final Object idValue = element().thatMatches(annotatedWith(Id.class).or(annotatedWith(EmbeddedId.class))).in(obj).value();
            logger.debug("getId - Id do objeto Entity " + String.valueOf(obj) + ": " + String.valueOf(idValue));
            return String.valueOf(idValue);
        } catch (Exception e) {
            logger.error("Erro no entityConverter " + obj.getClass().getName(), e);
        }
        return null;
    }
}
