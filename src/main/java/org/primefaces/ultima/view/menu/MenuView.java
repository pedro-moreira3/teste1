/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.ultima.view.menu;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

@Named
public class MenuView {
    
    private MenuModel model;

    @PostConstruct
	public void init() {
		model = new DefaultMenuModel();
		
		//First submenu
        DefaultSubMenu firstSubmenu = DefaultSubMenu.builder()
                .label("Dynamic Submenu").build();
        
        DefaultMenuItem item = DefaultMenuItem.builder()
                .value("External").build();
		item.setUrl("http://www.primefaces.org");
        item.setIcon("ui-icon-home");
		firstSubmenu.getElements().add(item);
        
        model.getElements().add(firstSubmenu);
		
		//Second submenu
		DefaultSubMenu secondSubmenu = DefaultSubMenu.builder()
		        .label("Dynamic Actions").build();

		item = DefaultMenuItem.builder()
		        .value("Save").build();
		item.setIcon("ui-icon-disk");
        item.setCommand("#{menuView.save}");
        item.setUpdate("messages");
        secondSubmenu.getElements().add(item);
        
        item = DefaultMenuItem.builder()
                .value("Delete").build();
        item.setIcon("ui-icon-close");
        item.setCommand("#{menuView.delete}");
        item.setAjax(false);
        secondSubmenu.getElements().add(item);
        
        item = DefaultMenuItem.builder()
                .value("Redirect").build();
        item.setIcon("ui-icon-search");
        item.setCommand("#{menuView.redirect}");
		secondSubmenu.getElements().add(item);

        model.getElements().add(secondSubmenu);
	}

	public MenuModel getModel() {
		return model;
	}	
    
    public void save() {
		addMessage("Success", "Data saved");
	}
	
	public void update() {
		addMessage("Success", "Data updated");
	}
	
	public void delete() {
		addMessage("Success", "Data deleted");
	}
	
	public void addMessage(String summary, String detail) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}
}
