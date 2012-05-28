package org.creditsms.plugins.paymentview.data.repository.hibernate;

import java.util.List;

import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;
import net.frontlinesms.data.Order;
import org.creditsms.plugins.paymentview.data.domain.LogMessage;
import org.creditsms.plugins.paymentview.data.repository.LogMessageDao;
import org.hibernate.criterion.DetachedCriteria;

@SuppressWarnings("unchecked")
public class HibernateLogMessageDao extends BaseHibernateDao<LogMessage> implements
    LogMessageDao {
	protected HibernateLogMessageDao() {
		super(LogMessage.class);
	}


	public List<LogMessage> getAllLogMessage() {
		return this.getHibernateTemplate().loadAll(LogMessage.class);
	}

	public List<LogMessage> getAllLogMessage(int startIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(LogMessage.Field.LOG_TIMESTAMP, Order.DESCENDING);
		return super.getList(criteria, startIndex, limit); 
	}

	public int getLogMessageCount() {
		return getAllLogMessage().size();
	}

	public void saveLogMessage(LogMessage logMessage) {
		super.saveWithoutDuplicateHandling(logMessage);
	}

}
