package org.creditsms.plugins.paymentview.ui.handler.client.dialogs;

import java.lang.reflect.Field;

import javax.persistence.Column;

import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.repository.CustomFieldDao;
import org.creditsms.plugins.paymentview.data.repository.CustomValueDao;

import net.frontlinesms.ui.ThinletUiEventHandler;
import net.frontlinesms.ui.UiGeneratorController;

public class CustomizeClientDBHandler implements ThinletUiEventHandler {
	private static final String XML_CUSTOMIZE_CLIENT_DB = "/ui/plugins/paymentview/clients/dialogs/dlgCustomizeClient.xml";

	private static final String COMPONENT_PNL_FIELDS = "pnlFields";
	private static final String COMPONENT_PARENT_PNL_FIELDS = "parentPnlFields";

	private UiGeneratorController ui;
	private Object dialogComponent;

	private Object compPanelFields;

	private int c;

	private Object compParentPanelFields;

	private CustomFieldDao customFieldDao;
	private CustomValueDao customValueDao;

	public CustomizeClientDBHandler(UiGeneratorController ui, CustomFieldDao customFieldDao, CustomValueDao customValueDao) {
		this.ui = ui;
		this.customFieldDao = customFieldDao;
		this.customValueDao = customValueDao;		
		init();
		refresh();
	} 

	public void refresh() {
		ui.remove(compPanelFields);
		ui.add(compParentPanelFields,compPanelFields);		
	}

	private void init() {
		dialogComponent = ui.loadComponentFromFile(XML_CUSTOMIZE_CLIENT_DB,
				this);
		compPanelFields = ui.find(dialogComponent, COMPONENT_PNL_FIELDS);
		compParentPanelFields = ui.find(dialogComponent, COMPONENT_PARENT_PNL_FIELDS);
		
		c = 0;
		for (Field field : Client.class.getDeclaredFields()) {
			if (field.isAnnotationPresent(Column.class)) {
				addField(field.getName());	
			}			
		}		
		refresh();
	}

	/**
	 * @return the customizeClientDialog
	 */
	public Object getDialog() {
		return dialogComponent;
	}

	/** Remove the dialog from view. */
	public void removeDialog() {
		this.removeDialog(this.dialogComponent);
	}

	/** Remove a dialog from view. */
	public void removeDialog(Object dialog) {
		this.ui.removeDialog(dialog);
	}

	public void showOtherFieldDialog() {
		ui.add(new OtherFieldHandler(ui, this).getDialog());
	}
	
	public void addField(String name) {
		this.addField(name, false);
	}
	
	public void addField(String name, boolean refresh) {
		Object label = ui.createLabel("Field "+ ++c); 
		Object txtfield = ui.createTextfield("fld"+name, name);
		ui.setColspan(txtfield, 2);
		ui.setColumns(txtfield, 50);
		ui.add(compPanelFields, label);
		ui.add(compPanelFields, txtfield);
		if (refresh){
			this.refresh();	
		}
	}

}
