package org.creditsms.plugins.paymentview.ui.handler.tabsettings;

import java.io.IOException;

import net.frontlinesms.events.EventBus;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.payment.PaymentService;
import net.frontlinesms.payment.PaymentServiceException;
import net.frontlinesms.payment.PaymentServiceStartedNotification;
import net.frontlinesms.payment.PaymentServiceStoppedNotification;
import net.frontlinesms.payment.safaricom.MpesaPaymentService;
import net.frontlinesms.payment.safaricom.MpesaPaymentService.BalanceFraudNotification;
import net.frontlinesms.ui.UiDestroyEvent;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.events.FrontlineUiUpateJob;
import net.frontlinesms.ui.handler.BaseTabHandler;

import org.creditsms.plugins.paymentview.PaymentViewPluginController;
import org.creditsms.plugins.paymentview.ui.handler.tabsettings.dialogs.UpdateAuthorizationCodeDialog;
import org.creditsms.plugins.paymentview.ui.handler.tabsettings.dialogs.steps.createnewsettings.MobilePaymentServiceSettingsInitialisationDialog;
import org.creditsms.plugins.paymentview.userhomepropeties.payment.balance.Balance.BalanceEventNotification;

public class SettingsTabHandler extends BaseTabHandler implements EventObserver{
	private static final String BTN_CREATE_NEW_SERVICE = "btn_createNewService";
	private static final String COMPONENT_SETTINGS_TABLE = "tbl_accounts";
	private static final String CONFIRM_CHECK_BALANCE = "message.confirm.checkbalance";
	private static final String XML_SETTINGS_TAB = "/ui/plugins/paymentview/settings/settingsTab.xml";

	private Object settingsTab;
	private Object settingsTableComponent;
	Object dialogConfirmation;
	private final PaymentViewPluginController pluginController;
	private EventBus eventBus;

	public SettingsTabHandler(UiGeneratorController ui, PaymentViewPluginController pluginController) {
		super(ui);
		this.pluginController = pluginController;
		
		eventBus = ui.getFrontlineController().getEventBus();
		eventBus.registerObserver(this);
		init();
	}

	public void createNew() {
		new MobilePaymentServiceSettingsInitialisationDialog(ui, pluginController).showDialog();
	}
	
	@Override
	protected Object initialiseTab() {
		settingsTab = ui.loadComponentFromFile(XML_SETTINGS_TAB, this);
		settingsTableComponent = ui.find(settingsTab, COMPONENT_SETTINGS_TABLE);
		return settingsTab;
	}
	
	public Object getRow(MpesaPaymentService paymentService) {
		Object row = ui.createTableRow(paymentService);
		Object paymentServiceName = ui.createTableCell(paymentService.toString());
		Object balance = ui.createTableCell(paymentService.getBalance().getBalanceAmount().toString());
		ui.add(row, paymentServiceName);
		ui.add(row, ui.createTableCell("Not configured"));
		ui.add(row, balance);
		return row;
	}

	@Override
	public void refresh() {
		ui.removeAll(settingsTableComponent);
		if (this.pluginController.getPaymentService() != null){
			ui.add(settingsTableComponent, getRow((MpesaPaymentService)this.pluginController.getPaymentService()));
		}
	}

	public void updateAccountBalance() throws PaymentServiceException, IOException {
		ui.remove(dialogConfirmation);
		Object selectedItem = this.ui.getSelectedItem(settingsTableComponent);
		if (selectedItem != null){
			PaymentService paymentService = ui.getAttachedObject(selectedItem, PaymentService.class);
			paymentService.checkBalance();
			ui.alert("Request has been sent. The Account balance will be updated shortly.");
		}else{
			ui.alert("Please select an account to update balance.");
		}
	}

	public final void checkBalance(String methodToBeCalled){
		dialogConfirmation = this.ui.showConfirmationDialog(methodToBeCalled, this, CONFIRM_CHECK_BALANCE);
	}
	
	public void updateAuthCode() {
		new UpdateAuthorizationCodeDialog(ui, pluginController).showDialog();
	}
	
	public void deleteAccount() {
		Object selectedItem = this.ui.getSelectedItem(settingsTableComponent);
		if (selectedItem != null) {
			PaymentService __paymentService = ui.getAttachedObject(selectedItem, PaymentService.class);
			//just before
			__paymentService.stop();
			//then notify listeners 
			eventBus.notifyObservers(new PaymentServiceStoppedNotification(__paymentService));
			//memory leaks?
			pluginController.setPaymentService(null);
		}else{
			ui.alert("Please select an account to delete.");
		}
	}

	public void notify(final FrontlineEventNotification notification) {
		new FrontlineUiUpateJob() {
			public void run() {
				if (notification instanceof BalanceEventNotification) {
					ui.alert(((BalanceEventNotification)notification).getMessage());
					SettingsTabHandler.this.refresh();
				}else if (notification instanceof PaymentServiceStartedNotification) {
					SettingsTabHandler.this.refresh();
					ui.setEnabled(ui.find(settingsTab, BTN_CREATE_NEW_SERVICE), false);
				}else if (notification instanceof PaymentServiceStoppedNotification) {
					SettingsTabHandler.this.refresh();
					ui.setEnabled(ui.find(settingsTab, BTN_CREATE_NEW_SERVICE), true);
				}else if(notification instanceof BalanceFraudNotification){
					ui.alert(((BalanceFraudNotification)notification).getMessage());
					SettingsTabHandler.this.refresh();
				}else if (notification instanceof UiDestroyEvent) {
					if(((UiDestroyEvent) notification).isFor(ui)) {
						ui.getFrontlineController().getEventBus().unregisterObserver(SettingsTabHandler.this);
					}
				}
			}
		}.execute();
	}
}
