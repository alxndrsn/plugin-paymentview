package org.creditsms.plugins.paymentview.ui.handler.tabanalytics.innertabs.steps.viewdashboard;

import java.util.Date;
import java.util.List;

import net.frontlinesms.data.events.DatabaseEntityNotification;
import net.frontlinesms.events.EventBus;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.ui.HomeTabEventNotification;
import net.frontlinesms.ui.UiDestroyEvent;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.events.FrontlineUiUpdateJob;
import net.frontlinesms.ui.handler.BasePanelHandler;

import org.creditsms.plugins.paymentview.PaymentViewPluginController;
import org.creditsms.plugins.paymentview.analytics.TargetAnalytics;
import org.creditsms.plugins.paymentview.data.domain.IncomingPayment;
import org.creditsms.plugins.paymentview.data.domain.ServiceItem;
import org.creditsms.plugins.paymentview.data.domain.Target;
import org.creditsms.plugins.paymentview.data.repository.IncomingPaymentDao;
import org.creditsms.plugins.paymentview.data.repository.TargetDao;
import org.creditsms.plugins.paymentview.ui.handler.importexport.TargetExportHandler;
import org.creditsms.plugins.paymentview.ui.handler.tabanalytics.dialogs.CreateAlertHandler;
import org.creditsms.plugins.paymentview.ui.handler.tabanalytics.innertabs.ViewDashBoardTabHandler;
import org.creditsms.plugins.paymentview.userhomepropeties.analytics.CreateAlertProperties;

public class CreateSettingsHandler extends BasePanelHandler implements EventObserver {
	private static final String PNL_CLIENT_TABLE_HOLDER = "pnlClientsTableHolder";
	private static final String XML_STEP_VIEW_CLIENTS = "/ui/plugins/paymentview/analytics/viewdashboard/stepviewclients.xml";
	private PaymentViewPluginController pluginController;
	private TargetAnalytics targetAnalytics;
	private Object clientTableHolder;
	private CreateSettingsTableHandler createSettingsTableHandler;
	private TargetDao targetDao;
	private IncomingPaymentDao incomingPaymentDao;
	private CreateAlertProperties createAlertProperties = CreateAlertProperties.getInstance();
	private final EventBus eventBus;

	public CreateSettingsHandler(UiGeneratorController ui,
			ViewDashBoardTabHandler viewDashBoardTabHandler,
			PaymentViewPluginController pluginController) {
		super(ui);
		this.eventBus = ui.getFrontlineController().getEventBus();
		
		this.pluginController = pluginController;
		
		targetAnalytics = new TargetAnalytics();
		targetAnalytics.setIncomingPaymentDao(pluginController.getIncomingPaymentDao());
		targetAnalytics.setTargetDao(pluginController.getTargetDao());
		targetDao = pluginController.getTargetDao();
		incomingPaymentDao = pluginController.getIncomingPaymentDao();
		
		eventBus.registerObserver(this);
		init();
	}

	
	private void init() {
		this.loadPanel(XML_STEP_VIEW_CLIENTS);
		clientTableHolder = ui.find(this.getPanelComponent(), PNL_CLIENT_TABLE_HOLDER);
		
		createSettingsTableHandler = new CreateSettingsTableHandler((UiGeneratorController) ui, pluginController, this);
		ui.add(clientTableHolder, createSettingsTableHandler.getClientsTablePanel());
		getTargetAlerts();
		refresh();
	}
	
	private void getTargetAlerts() {
		if (analyticsAlertsOn()) {
			List<Target> tgtList = targetDao.getAllActiveTargets();
			for (Target target: tgtList) {
				long endTime = target.getEndDate().getTime();
				if (createAlertProperties.getMissesDeadline()) {
					if (new Date().getTime() > endTime) {
						eventBus.notifyObservers(new HomeTabEventNotification(HomeTabEventNotification.Type.RED, 
								"Missed deadline and is overdue with savings target : " + target.getAccount().getClient().getFullName()));					
					}
				}
				if (new Date().getTime() < endTime) {
					if (createAlertProperties.getTwksWithoutPay()) {
						if(getDaysDormant(target)>=14 && getDaysDormant(target)<30 ) {
							eventBus.notifyObservers(new HomeTabEventNotification(HomeTabEventNotification.Type.AMBER, 
									"Have not paid for 2 weeks : " + target.getAccount().getClient().getFullName()));	
						}
					}
					if (createAlertProperties.getA_mnthWithoutPay()) {
						if(getDaysDormant(target)>=30) {
							eventBus.notifyObservers(new HomeTabEventNotification(HomeTabEventNotification.Type.AMBER, 
									"Have not paid for a month : " + target.getAccount().getClient().getFullName()));	
						}
					}					
				}
			}
		}
	}
	
	private Long getDateDiffDays(long startTime, long endTime){
	    long diff = endTime - startTime;
		long targetDays = diff / (1000 * 60 * 60 * 24);
		
		return targetDays +1;
	}
	
	public Long getDaysDormant(Target tgt){
		Date startTime = null;
		List<IncomingPayment> lstIncomingPayment = incomingPaymentDao.getActiveIncomingPaymentsByTarget(tgt.getId());
		if(lstIncomingPayment!=null && lstIncomingPayment.size()>0){
			startTime = targetAnalytics.getLastDatePaid(tgt.getId());
	    }
		if(startTime == null) {
			startTime = tgt.getStartDate();
		}
		
		return Math.max(0, getDateDiffDays(startTime.getTime(), System.currentTimeMillis()));
	}
	
	private boolean analyticsAlertsOn() {
		return createAlertProperties.isAlertOn();
	}
	
	public void refresh() {
		this.createSettingsTableHandler.refresh();
	}
	
	public void createAlert() {
		ui.add(new CreateAlertHandler((UiGeneratorController) ui).getDialog());
	}

	public void export() {
		new TargetExportHandler((UiGeneratorController) ui, pluginController).showWizard();
	}

	public void showDateSelecter(Object textField) {
		((UiGeneratorController) ui).showDateSelecter(textField);
	}
	
	public void notify(final FrontlineEventNotification notification) {
		if (notification instanceof DatabaseEntityNotification) {
			if (notification instanceof DatabaseEntityNotification){
				Object entity = ((DatabaseEntityNotification<?>) notification).getDatabaseEntity();
				if (entity instanceof IncomingPayment || entity instanceof ServiceItem) {
					new FrontlineUiUpdateJob() {
						public void run() {
							refresh();
						}
					}.execute();
				}
			}
		} else if(notification instanceof UiDestroyEvent) {
			eventBus.unregisterObserver(this);
		}
	}
}
