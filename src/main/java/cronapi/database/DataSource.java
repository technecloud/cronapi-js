package cronapi.database;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;

import cronapi.Utils;

/**
 * Class database manipulation, responsible for querying, inserting, 
 * updating and deleting database data procedurally, allowing paged 
 * navigation and setting page size.
 * 
 * @author robson.ataide
 * @version 1.0
 * @since 2017-04-26
 *
 */
public class DataSource {

	private String entity;
	private String entityFullName;
	private Class<?> domainClass;
	private String filter;
	private Object[] params;
	private List<Object> results;
	private int pageSize;
	private Page page;
	private int index;
	private int current;
	private JpaRepository<Object, String> repository;
	private PageRequest pageRequest;

	/** Init a datasource with a page size equals 20
	* 
	* @param jndiDataSource - jndi name datasource
	* @param jpqlInstruction - query instruction instruction 
	*/
	public DataSource(String entity) {
		this(entity, 10);
	}

	public DataSource(String entity, int pageSize) {
		this.entityFullName = entity;
		//get simple class name, removing the her package 
		this.entity = entity.split("\\.")[entity.split("\\.").length - 1];
		this.pageSize = pageSize;
		this.pageRequest = new PageRequest(0, pageSize);

		//initialize dependencies and necessaries objects
		this.results = new ArrayList<Object>();
		this.instantiateRepository();
		this.createQuery();
	}

	private String createQuery() {

		String query = "select o from {0} o {1}";

		if (this.filter != null && "".equals(this.filter)) {
			this.filter = "where " + this.filter;
		} else {
			this.filter = "";
		}

		return String.format(query, this.entity, this.filter);

	}

	private void instantiateRepository() {
		try {
			ListableBeanFactory factory = (ListableBeanFactory) ApplicationContextHolder.getContext();
			Repositories repositories = new Repositories(factory);
			domainClass = Class.forName(this.entityFullName);
			if (repositories.hasRepositoryFor(domainClass)) {
				this.repository = (JpaRepository) repositories.getRepositoryFor(domainClass);
			}

		} catch (ClassNotFoundException cnfex) {
			this.repository = null;
		} catch (ClassCastException ccex) {
			this.repository = null;
		}
	}

	// Begins database queries

	public Object[] fetch() {
		this.page = this.repository.findAll(this.pageRequest);
		this.results.addAll(this.page.getContent());
		this.current = -1;
		return this.page.getContent().toArray();
	}

	public void insert() {
		Page lastPage = this.page;
		while (!lastPage.isLast()) {
			lastPage = this.repository.findAll(this.page.nextPageable());
			if (lastPage.isLast())
				this.results.addAll(lastPage.getContent());
		}
		try {
			Object newObj = Utils.clearObject(this.results.get(0));
			this.results.add(newObj);
			this.current = this.results.size() - 1;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void save() {
	  Object toSave = this.getObject();
	  this.repository.save(toSave);
	}

	public void remove() {
	  Object toRemove = this.getObject();
	  this.repository.delete(toRemove);
	}

	// Ends database queries 

	public void updateField(String fieldName, Object fieldValue) {
		try {
			Method setMethod = Utils.findMethod(getObject(), "set" + fieldName);
			if (setMethod != null) {
				setMethod.invoke(getObject(), fieldValue);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void updateFields(Object[][] fields) {
	  try {
	    for(Object[] oArray : fields){
  			Method setMethod = Utils.findMethod(getObject(), "set" + oArray[0]);
  			if (setMethod != null) {
  				setMethod.invoke(getObject(), oArray[1]);
  			}
	    }
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public Object getObject() {
		return this.results.get(this.current);
	}

	public Object getObject(String fieldName) {
		try {
			Method getMethod = Utils.findMethod(getObject(), "get" + fieldName);
			if (getMethod != null)
				return getMethod.invoke(getObject());
			return null;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public boolean next() {
		if (this.results.size() > (this.current + 1))
			this.current++;
		else {
			if (this.page.hasNext()) {
				this.page = this.repository.findAll(this.page.nextPageable());
				this.results.addAll(this.page.getContent());
				this.current++;
			} else {
				return false;
			}
		}
		return true;
	}

	public Page getPage() {
		return this.page;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		this.pageRequest = new PageRequest(0, pageSize);
		this.current = -1;
		this.results.clear();
	}

	public void filter(String conditional, Object... params) {
		this.filter = conditional;
		this.params = params;
	}
}
