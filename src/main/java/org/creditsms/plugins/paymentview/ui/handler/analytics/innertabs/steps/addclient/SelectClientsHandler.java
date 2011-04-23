package org.creditsms.plugins.paymentview.ui.handler.analytics.innertabs.steps.addclient;

import java.util.List;

import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.handler.BasePanelHandler;
import net.frontlinesms.ui.handler.ComponentPagingHandler;
import net.frontlinesms.ui.handler.PagedComponentItemProvider;
import net.frontlinesms.ui.handler.PagedListDetails;

import org.creditsms.plugins.paymentview.data.domain.Account;
import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.repository.ClientDao;
import org.creditsms.plugins.paymentview.ui.handler.analytics.innertabs.AddClientTabHandler;

public class SelectClientsHandler extends BasePanelHandler implements
		PagedComponentItemProvider {
	private static final String COMPONENT_CLIENT_TABLE = "tbl_clients";
	private static final String XML_STEP_SELECT_CLIENT = "/ui/plugins/paymentview/analytics/createdashboard/stepselectclients.xml";
	private static final String COMPONENT_PANEL_CLIENT_LIST = "pnl_clients";
	private Object clientsTableComponent;
	private String clientFilter = "";
	private ClientDao clientDao;
	private final AddClientTabHandler addClientTabHandler;
	private ComponentPagingHandler clientsTablePager;
	private Object pnlClientsList;

	protected SelectClientsHandler(UiGeneratorController ui,
			ClientDao clientDao, AddClientTabHandler addClientTabHandler) {
		super(ui);
		this.addClientTabHandler = addClientTabHandler;
		this.clientDao = clientDao;
		this.loadPanel(XML_STEP_SELECT_CLIENT);
		initialise();
	}

	private void initialise() {
		clientsTableComponent = ui.find(this.getPanelComponent(), COMPONENT_CLIENT_TABLE);
		clientsTablePager = new ComponentPagingHandler((UiGeneratorController) ui, this, clientsTableComponent);
		pnlClientsList = ui.find(this.getPanelComponent(), COMPONENT_PANEL_CLIENT_LIST);
		this.ui.add(pnlClientsList, this.clientsTablePager.getPanel());
		refresh();
	}

	private void refresh() {
		this.updateContactList();
	}

	public void updateContactList() {
		this.clientsTablePager.setCurrentPage(0);
		this.clientsTablePager.refresh();
	}

	private Object getRow(Client client) {
		Object row = ui.createTableRow(client);

		ui.add(row,
				ui.createTableCell(client.getFirstName() + " "
						+ client.getOtherName()));
		ui.add(row, ui.createTableCell(client.getPhoneNumber()));
		String accountStr = "";
		for (Account a : client.getAccounts()) {
			accountStr += (Long.toString(a.getAccountNumber()) + ", ");
		}
		ui.add(row, ui.createTableCell(accountStr));
		return row;
	}

	public Object getPanelComponent() {
		return super.getPanelComponent();
	}

	public void next() {
		addClientTabHandler
				.setCurrentStepPanel(new CreateSettingsHandler(
						(UiGeneratorController) ui, clientDao, addClientTabHandler).getPanelComponent());
	}

	public void previous() {
		addClientTabHandler
				.setCurrentStepPanel(new SelectTargetSavingsHandler(
						(UiGeneratorController) ui, clientDao, addClientTabHandler)
						.getPanelComponent());
	}

	public void selectService() {
		previous();
	}

	public void targetedSavings() {
		previous();
	}

	public PagedListDetails getListDetails(Object list, int startIndex,
			int limit) {
		if (list == this.clientsTableComponent) {
			return getClientListDetails(startIndex, limit);
		} else {
			throw new IllegalStateException();
		}
	}

	private PagedListDetails getClientListDetails(int startIndex, int limit) {
		List<Client> clients = null;
		if (this.clientFilter.equals("")) {
			clients = this.clientDao.getAllClients();
		} else {
			clients = this.clientDao.getClientsByName(clientFilter);
		}

		int totalItemCount = this.clientDao.getClientCount();
		Object[] listItems = toThinletComponents(clients);

		return new PagedListDetails(totalItemCount, listItems);
	}

	private Object[] toThinletComponents(List<Client> clients) {
		Object[] components = new Object[clients.size()];
		for (int i = 0; i < components.length; i++) {
			Client c = clients.get(i);
			components[i] = getRow(c);
		}
		return components;
	}
}
