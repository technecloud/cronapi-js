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

public class DataSourceFilter {
  
  public LinkedList<DataSourceFilterItem> items = new LinkedList<>();
  public String type = "AND";
  
  private String appliedJpql;
  private Var[] appliedParams;
  
  public DataSourceFilter(String filter) {
    if(!filter.trim().isEmpty()) {
      
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
            items.add(new DataSourceFilter.DataSourceFilterItem("*", Var.valueOf(pair[0]), type));
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
    
  }
  
  public String getAppliedJpql() {
    return appliedJpql;
  }
  
  public Var[] getAppliedParams() {
    return appliedParams;
  }
  
  public List<String> findSearchables(Object obj) {
    Field[] fields = obj instanceof Class ? ((Class)obj).getDeclaredFields() : obj.getClass().getDeclaredFields();
    List<String> searchable = new ArrayList<>();
    for(Field f : fields) {
      Annotation[] annotations = f.getDeclaredAnnotations();
      for(int i = 0; i < annotations.length; i++) {
        if(annotations[i].annotationType().equals(CronappSecurity.class)) {
          CronappSecurity security = (CronappSecurity)annotations[i];
          String authoritiesStr = security.filter();
          String[] authorities;
          if(authoritiesStr != null && !authoritiesStr.trim().isEmpty()) {
            authorities = authoritiesStr.trim().split(";");
            boolean authorized = false;
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
            
            if(authorized) {
              searchable.add(f.getName());
            }
          }
        }
      }
    }
    return searchable;
  }
  
  public void applyTo(Class domainClass, String jpql, Var[] params) {
    if(items.size() == 0) {
      this.appliedParams = params;
      this.appliedJpql = jpql;
      return;
    }
    
    String alias = "e";
    boolean hasWhere = false;
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

    if (parser.getParseTree().getWhereNode() != null) {
      hasWhere = true;
    }

    List<String> searchables = findSearchables(domainClass);

    if(items.size() == 1 && items.get(0).key == "*") {
      if(searchables.isEmpty()) {
        throw new RuntimeException(Messages.getString("notAllowed"));
      }
      else {
        Var value = items.get(0).value;
        String type = items.get(0).type;
        items = new LinkedList<>();
        for(String f : searchables) {
          items.add(new DataSourceFilterItem(f, value, type));
        }
      }
    }
    
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
    for(DataSourceFilterItem item : items) {
      if(add) {
        jpql += " " + type + " ";
      }
      add = true;
      
      jpql += alias + "." + item.key + " " + item.type + " :p" + i;
      newParams[i] = item.value;
      i++;

      if (!searchables.contains(item.key)) {
        throw new RuntimeException(Messages.getString("notAllowed"));
      }
    }
    
    jpql += ")";
    this.appliedParams = newParams;
    this.appliedJpql = jpql;
  }
  
  public static class DataSourceFilterItem {
    public String key;
    public Var value;
    public String type = "=";
    
    public DataSourceFilterItem(String key, Var value, String type) {
      this.key = key;
      this.value = value;
      this.type = type;
    }
  }
}
