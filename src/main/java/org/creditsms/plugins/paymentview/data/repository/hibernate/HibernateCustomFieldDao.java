package org.creditsms.plugins.paymentview.data.repository.hibernate;

import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import org.creditsms.plugins.paymentview.data.domain.CustomField;
import org.creditsms.plugins.paymentview.data.repository.CustomFieldDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class HibernateCustomFieldDao extends BaseHibernateDao<CustomField>
		implements CustomFieldDao {
	protected HibernateCustomFieldDao() {
		super(CustomField.class);
	}

	public void deleteCustomField(CustomField customField) {
		super.delete(customField);
	}

	public List<CustomField> getAllCustomFields() {
		return super.getAll();
	}

	public List<CustomField> getAllCustomFields(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	public CustomField getCustomFieldById(long id) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq("id", id));
		return super.getUnique(criteria);
	}

	public int getCustomFieldCount() {
		return super.getAll().size();
	}

	public List<CustomField> getCustomFieldsByName(String strName) {
		DetachedCriteria criteria = super.getCriterion().add(
				Restrictions.disjunction().add(
						Restrictions.ilike("strName", strName.trim(),
								MatchMode.ANYWHERE)));
		return super.getList(criteria);
	}

	public List<CustomField> getCustomFieldsByName(String strName,
			int startIndex, int limit) {
		DetachedCriteria criteria = super.getCriterion().add(
				Restrictions.disjunction().add(
						Restrictions.ilike("strName", strName.trim(),
								MatchMode.ANYWHERE)));
		return super.getList(criteria, startIndex, limit);
	}

	public void saveCustomField(CustomField customField)
			throws DuplicateKeyException {
		super.save(customField);
	}

	public void updateCustomField(CustomField customField)
			throws DuplicateKeyException {
		super.update(customField);

	}
}
