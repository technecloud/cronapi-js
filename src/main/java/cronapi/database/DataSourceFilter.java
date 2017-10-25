package cronapi.database;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cronapi.i18n.Messages;
import org.eclipse.persistence.internal.jpa.parsing.DotNode;
import org.eclipse.persistence.internal.jpa.parsing.SelectNode;
import org.eclipse.persistence.internal.jpa.parsing.VariableNode;
import org.eclipse.persistence.internal.jpa.parsing.jpql.JPQLParser;
import org.springframework.security.core.GrantedAuthority;

import cronapi.RestClient;
import cronapi.Var;
import cronapi.rest.security.CronappSecurity;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

public class DataSourceFilter {
  
  private LinkedList<DataSourceFilterItem> items = new LinkedList<>();
  private LinkedList<DataSourceOrderItem> orders = new LinkedList<>();
  private String type = "AND";
  
  private String appliedJpql;
  private Var[] appliedParams;
  
  private DataSourceFilter(String filter, String order) {
    if(filter != null && !filter.trim().isEmpty()) {
      
      String[] values = filter.trim().split(";");
      if(values.length > 0) {
        for(String v : values) {
          String[] pair = null;
          String type;
          if(v.contains("@=")) {
            pair = v.trim().split("@=");
            type = "LIKE";
          }
          else {
            pair = v.trim().split("=");
            type = "=";
          }
          
          if(values.length == 1 && pair.length == 1) {
            items.add(new DataSourceFilter.DataSourceFilterItem("*", Var.valueOf(pair[0]), "LIKE"));
            break;
          }
          
          if(pair.length > 0 && !pair[0].trim().isEmpty()) {
            if(pair.length == 1) {
              items.add(new DataSourceFilter.DataSourceFilterItem(pair[0], Var.VAR_NULL, type));
            }
            if(pair.length > 1) {
              items.add(new DataSourceFilter.DataSourceFilterItem(pair[0], Var.valueOf(pair[1]), type));
            }
          }
        }
      }
    }
    
    if(order != null && !order.trim().isEmpty()) {
      
      String[] values = order.trim().split(";");
      if(values.length > 0) {
        for(String v : values) {
          String[] pair = v.trim().split("\\|");
          
          if(pair.length == 1) {
            orders.add(new DataSourceFilter.DataSourceOrderItem(pair[0], "ASC"));
          }
          
          if(pair.length == 2) {
            orders.add(new DataSourceFilter.DataSourceOrderItem(pair[0], pair[1]));
          }
        }
      }
    }
    
  }
  
  public static DataSourceFilter getInstance(String filter, String order, String filterType) {
    if((filter != null && !filter.trim().isEmpty()) || (order != null && !order.trim().isEmpty())) {
      DataSourceFilter dsFilter = new DataSourceFilter(filter, order);
      if(filterType != null)
        dsFilter.setType(filterType);
      
      return dsFilter;
    }
    
    return null;
  }
  
  public LinkedList<DataSourceFilterItem> getItems() {
    return items;
  }
  
  public void setItems(LinkedList<DataSourceFilterItem> items) {
    this.items = items;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    if(type.equalsIgnoreCase("or") || type.equalsIgnoreCase("and")) {
      this.type = type;
    }
  }
  
  public String getAppliedJpql() {
    return appliedJpql;
  }
  
  public Var[] getAppliedParams() {
    return appliedParams;
  }
  
  public List<String> findSearchables(Object obj, boolean filterWithAnnotation) {
    Field[] fields = obj instanceof Class ? ((Class)obj).getDeclaredFields() : obj.getClass().getDeclaredFields();
    List<String> searchable = new ArrayList<>();

    EntityManager em = TransactionManager.getEntityManager((Class)obj);
    EntityType type = em.getMetamodel().entity((Class)obj);

    for(Field f : fields) {
      boolean contains = false;
      for (Object attrObj : type.getAttributes()) {
        SingularAttribute attr = (SingularAttribute) attrObj;
        if (attr.getName().equalsIgnoreCase(f.getName())) {
          contains = true;
          continue;
        }
      }

      if (!contains)
        continue;

      Annotation annotation = f.getAnnotation(CronappSecurity.class);
      boolean authorized = true;
      if(annotation != null) {
        CronappSecurity security = (CronappSecurity)annotation;
        String authoritiesStr = security.filter();
        String[] authorities;
        if(authoritiesStr != null && !authoritiesStr.trim().isEmpty()) {
          authorized = false;
          authorities = authoritiesStr.trim().split(";");
          for(String role : authorities) {
            if(role.equalsIgnoreCase("authenticated")) {
              authorized = RestClient.getRestClient().getUser() != null;
              if(authorized)
                break;
            }
            if(role.equalsIgnoreCase("permitAll") || role.equalsIgnoreCase("public")) {
              authorized = true;
              break;
            }
            for(GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
              if(role.equalsIgnoreCase(authority.getAuthority())) {
                authorized = true;
                break;
              }
            }
            
          }
        }
      }
      if(authorized) {
        if (filterWithAnnotation) {
          if (f.getAnnotation(CronappSecurity.class) != null)
            searchable.add(f.getName());
        } else {
          searchable.add(f.getName());
        }
      }
    }
    return searchable;
  }
  
  public void applyTo(Class domainClass, String jpql, Var[] params) {
    this.appliedParams = params;
    this.appliedJpql = jpql;
    
    if(items.size() == 0 && orders.size() == 0) {
      return;
    }
    
    String alias = "e";
    boolean hasWhere = false;
    boolean hasOrder = false;
    JPQLParser parser = JPQLParser.buildParserFor(jpql);
    parser.parse();
    if(parser.getParseTree().getQueryNode().isSelectNode()) {
      SelectNode selectNode = (SelectNode)parser.getParseTree().getQueryNode();
      if(selectNode.getSelectExpressions().size() > 0) {
        if(selectNode.getSelectExpressions().get(0) instanceof DotNode) {
          DotNode dotNode = (DotNode)selectNode.getSelectExpressions().get(0);
          alias = dotNode.getAsString();
        }
        
        if(selectNode.getSelectExpressions().get(0) instanceof VariableNode) {
          VariableNode dotNode = (VariableNode)selectNode.getSelectExpressions().get(0);
          alias = dotNode.getAsString();
        }
      }
    }
    
    if(parser.getParseTree().getWhereNode() != null) {
      hasWhere = true;
    }
    
    if(parser.getParseTree().getOrderByNode() != null) {
      hasOrder = true;
    }
    
    List<String> searchables = null;
    
    if(items.size() == 1 && items.get(0).key == "*") {
      searchables = findSearchables(domainClass, true);

      if(searchables.isEmpty()) {
        throw new RuntimeException(Messages.getString("notAllowed"));
      }
      else {
        Var value = items.get(0).value;
        String type = items.get(0).type;
        items = new LinkedList<>();
        this.type = "OR";
        for(String f : searchables) {
          items.add(new DataSourceFilterItem(f, value, type));
        }
      }
    } else {
      searchables = findSearchables(domainClass, false);
    }
    
    if(items.size() > 0) {
      if(!hasWhere) {
        jpql += " where (";
      }
      else {
        jpql += " AND (";
      }
      
      Var[] newParams = new Var[params.length + items.size()];
      for(int j = 0; j < params.length; j++) {
        newParams[j] = params[j];
      }
      int i = params.length;
      boolean add = false;
      setTypeBasedOnItemsValue();
      for(DataSourceFilterItem item : items) {
        if(add) {
          jpql += " " + type + " ";
        }
        add = true;
        
        jpql += alias + "." + item.key + " " + item.type + " :p" + i;
        newParams[i] = item.value;
        i++;
        
        if(!searchables.contains(item.key)) {
          throw new RuntimeException(Messages.getString("notAllowed"));
        }
      }
      
      jpql += ")";
      
      this.appliedParams = newParams;
    }
    
    if(orders.size() > 0) {
      if(!hasOrder) {
        jpql += " ORDER BY ";
      }
      else {
        jpql += ", ";
      }
      
      boolean add = false;
      for(DataSourceOrderItem order : orders) {
        if(add) {
          jpql += ", ";
        }
        add = true;
        jpql += alias + "." + order.key + " " + order.type;
      }
    }
    
    this.appliedJpql = jpql;
  }
  
  private void setTypeBasedOnItemsValue() {
    boolean isSameValue = true;
    for(DataSourceFilterItem item : items) {
      for(DataSourceFilterItem itemToCheck : items) {
        if (!item.value.getObjectAsString().equals(itemToCheck.value.getObjectAsString())) {
          isSameValue = false;
          break;
        }
      }
      break;
    }
    if (isSameValue)
      this.type = "or";
    else
      this.type = "and";
    
  }
  
  public static class DataSourceFilterItem {
    public String key;
    public Var value;
    public String type = "=";
    
    public DataSourceFilterItem(String key, Var value, String type) {
      this.key = key;
      this.value = value;
      if(type.equalsIgnoreCase("=") || type.equalsIgnoreCase("like"))
        this.type = type;
    }
  }
  
  public static class DataSourceOrderItem {
    public String key;
    public String type = "ASC";
    
    public DataSourceOrderItem(String key, String type) {
      this.key = key;
      if(type.equalsIgnoreCase("asc") || type.equalsIgnoreCase("desc"))
        this.type = type;
    }
  }
}
