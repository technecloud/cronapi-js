package cronapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import cronapi.i18n.Messages;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Var implements Comparable, JsonSerializable {

  public enum Type {
    STRING, INT, DOUBLE, LIST, NULL, UNKNOWN, BOOLEAN, DATETIME
  };

  private String id;
  private Type _type;
  private Object _object;
  private boolean modifiable = true;
  private boolean created = false;
  private static final NumberFormat _formatter = new DecimalFormat("#.#####");

  public static final Var VAR_NULL = new Var(null, false);
  public static final Var VAR_TRUE = new Var(true, false);
  public static final Var VAR_FALSE = new Var(false, false);
  public static final Var VAR_ZERO = new Var(0, false);
  public static final Var VAR_ONE = new Var(1, false);
  public static final Var VAR_NEGATIVE_ONE = new Var(-1, false);
  public static final Var VAR_EMPTY = new Var("", false);
  public static final Var VAR_DATE_ZERO;

  static {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1980, 1, 1, 0, 0, 0);
    VAR_DATE_ZERO = new Var(calendar, false);
  }

  /**
   * Construct a Var with an NULL type
   *
   */
  public Var() {
    _type = Type.NULL;
    created = true;
  }

  /**
   * Construct a Var and assign its contained object to that specified.
   *
   * @param object The value to set this object to
   */
  public Var(String id, Object object) {
    this.id = id;
    setObject(object);
  }

  /**
   * Construct a Var and assign its contained object to that specified.
   *
   * @param object The value to set this object to
   */
  public Var(Object object) {
    setObject(object);
  }

  public Var(Object object, boolean modifiable) {
    setObject(object);
    this.modifiable = modifiable;
  }

  /**
   * Construct a Var from a given Var
   *
   * @param var var to construct this one from
   */
  public Var(Var var) {
    _type = Type.UNKNOWN;
    if (var!=null) {
      this.id = var.id;
      setObject(var.getObject());
    }
  }

  /**
   * Set the value of the underlying object. Note that the type of Var will be
   * determined when setObject is called.
   *
   * @param val the value to set this Var to
   */
  public void setObject(Object val) {
    if (created && !modifiable) {
      throw new RuntimeException(Messages.getString("NotModifiable"));
    }
    this._object = val;
    inferType();
    // make sure each element of List is Var if type is list
    if (_type.equals(Var.Type.LIST)) {
      LinkedList<Var> myList = new LinkedList<>();
      for (Object obj : this.getObjectAsList()) {
        myList.add(Var.valueOf(obj));
      }
      this._object = myList;
    }

    created = true;
  }

  /**
   * Static constructor to make a var from some value.
   *
   * @param val some value to construct a var around
   * @return the Var object
   */
  public static Var valueOf(Object val) {
    if (val instanceof Var)
      return (Var) val;

    if (val instanceof Boolean) {
      if (((Boolean) val)) {
        return VAR_TRUE;
      } else {
        return VAR_FALSE;
      }
    }

    if (val == null) {
      return VAR_NULL;
    }

    return new Var(val);
  }

  public static Var valueOf(String id, Object val) {
    if (val instanceof Var && Objects.equals(((Var) val).getId(), id))
      return (Var) val;

    return new Var(id, val);
  }

  /**
   * Get the type of the underlying object
   *
   * @return Will return the object's type as defined by Type
   */
  public Type getType() {
    return _type;
  }

  public String getId() {
    return this.id;
  }

  /**
   * Get the contained object
   *
   * @return the object
   */
  public Object getObject() {
    return _object;
  }

  public Object getObject(Class type) {
    if (type == String.class || type == StringBuilder.class || type == StringBuffer.class
        || type == Character.class) {
      return getObjectAsString();
    } else if (type == Boolean.class) {
      return getObjectAsBoolean();
    } else if (type == Date.class) {
      return getObjectAsDateTime().getTime();
    } else if (type == Calendar.class) {
      return getObjectAsDateTime();
    } else if (type == Long.class) {
      return getObjectAsLong();
    } else if (type == Integer.class) {
      return getObjectAsInt();
    } else if (type == Double.class) {
      return getObjectAsDouble();
    } else if (type == Float.class) {
      return getObjectAsDouble().floatValue();
    } else if (type == BigDecimal.class) {
      return new BigDecimal(getObjectAsDouble());
    } else if (type == BigInteger.class) {
      return BigInteger.valueOf(getObjectAsLong());
    } else {

      if (_object instanceof Map && type != Map.class) {
        ObjectMapper mapper = new ObjectMapper();
        ((Map<?,?>) _object).remove("$$hashKey");
        return mapper.convertValue(_object, type);
      }

      return getObject();
    }
  }

  /**
   * Clone Object
   *
   * @return a new object equal to this one
   */
  public Object cloneObject() {
    Var tempVar = new Var(this);
    return tempVar.getObject();
  }

  /**
   * Get object as an int. Does not make sense for a "LIST" type object
   *
   * @return an integer whose value equals this object
   */
  public Integer getObjectAsInt() {
    switch (getType()) {
      case STRING:
        return Integer.parseInt(((String) getObject()));
      case INT:
        return ((Long) getObject()).intValue();
      case BOOLEAN:
        return ((Boolean) getObject()) ? 1 : 0;
      case DOUBLE:
        return new Double((double) getObject()).intValue();
      case DATETIME:
        return (int)(((Calendar) getObject()).getTimeInMillis());
      case LIST:
        // has no meaning
        break;
      default:
        // has no meaning
        break;
    }
    return 0;
  }

  /**
   * Get object as an int. Does not make sense for a "LIST" type object
   *
   * @return an integer whose value equals this object
   */
  public Long getObjectAsLong() {
    switch (getType()) {
      case STRING:
        return Long.parseLong((String) getObject());
      case INT:
        return (Long) getObject();
      case BOOLEAN:
        return ((Boolean) getObject()) ? 1L : 0L;
      case DOUBLE:
        return new Double((double) getObject()).longValue();
      case DATETIME:
        return (Long)((Calendar) getObject()).getTimeInMillis();
      case LIST:
        // has no meaning
        break;
      default:
        // has no meaning
        break;
    }
    return 0L;
  }

  /**
   * Get object as an boolean.
   *
   * @return an bool whose value equals this object
   */
  public Calendar getObjectAsDateTime() {
    switch (getType()) {
      case STRING:
        String s = (String) getObject();
        return Utils.toCalendar(s, null);
      case INT:
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(getObjectAsInt());
        return c;
      case DOUBLE:
        Calendar cd = Calendar.getInstance();
        cd.setTimeInMillis(getObjectAsInt());
        return cd;
      case DATETIME:
        return (Calendar) getObject();
      case LIST:
        // has no meaning
        break;
      default:
        // has no meaning
        break;
    }
    return null;
  }

  /**
   * Get object as an boolean.
   *
   * @return an bool whose value equals this object
   */
  public Boolean getObjectAsBoolean() {
    switch (getType()) {
      case STRING:
        String s = (String) getObject();
        if (s.equals("1") || s.equalsIgnoreCase("true")) {
          s = "true";
        } else {
          s = "false";
        }
        return Boolean.valueOf(s);
      case INT:
        return (Long) getObject() > 0;
      case BOOLEAN:
        return (boolean) getObject();
      case DOUBLE:
        return new Double((double) getObject()).intValue() > 0;
      case DATETIME:
        //has no meaning
        break;
      case LIST:
        // has no meaning
        break;
      default:
        // has no meaning
        break;
    }
    return false;
  }

  /**
   * Get object as a double. Does not make sense for a "LIST" type object.
   *
   * @return a double whose value equals this object
   */
  public Double getObjectAsDouble() {
    switch (getType()) {
      case STRING:
        return Double.parseDouble((String) getObject());
      case INT:
        return new Long((Long) getObject()).doubleValue();
      case BOOLEAN:
        return ((boolean) getObject()) ? 1.0 : 0.0;
      case DOUBLE:
        return (double) getObject();
      case DATETIME:
        return (double)((Calendar) getObject()).getTimeInMillis();
      case LIST:
        // has no meaning
        break;
      default:
        // has no meaning
        break;
    }
    return 0.0;
  }

  /**
   * Get object as a string.
   *
   * @return The string value of the object. Note that for lists, this is a
   * comma separated list of the form {x,y,z,...}
   */
  public String getObjectAsString() {
    return this.toString();
  }

  /**
   * Get the object as a list.
   *
   * @return a LinkedList whose elements are of type Var
   */
  public LinkedList<Var> getObjectAsList() {
    return (LinkedList<Var>) getObject();
  }

  public Iterator<Var> iterator() {
    return getObjectAsList().iterator();
  }

  /**
   * If this object is a linked list, then calling this method will return the
   * Var at the index indicated
   *
   * @param index the index of the Var to read (0 based)
   * @return the Var at that index
   */
  public Var get(int index) {
    return ((LinkedList<Var>) getObject()).get(index);
  }

  /**
   * If this object is a linked list, then calling this method will return the
   * size of the linked list.
   *
   * @return size of list
   */
  public int size() {
    return ((LinkedList<Var>) getObject()).size();
  }

  public int length() {
    return getObjectAsString().length();
  }

  public void trim() {
    setObject(getObjectAsString().trim());
  }

  public static Var newList() {
    return new Var(new LinkedList<>());
  }

  /**
   * Set the value of of a list at the index specified. Note that this is only
   * value if this object is a list and also note that index must be in
   * bounds.
   *
   * @param index the index into which the Var will be inserted
   * @param var the var to insert
   */
  public void set(int index, Var var) {
    ((LinkedList<Var>) getObject()).add(index, var);
  }

  /**
   * Add all values from one List to another. Both lists are Var objects that
   * contain linked lists.
   *
   * @param var The list to add
   */
  public void addAll(Var var) {
    ((LinkedList<Var>) getObject()).addAll(var.getObjectAsList());
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 43 * hash + Objects.hashCode(this._type);
    hash = 43 * hash + Objects.hashCode(this._object);
    return hash;
  }

  /**
   * Test to see if this object equals another one. This is done by converting
   * both objects to strings and then doing a string compare.
   *
   * @param obj
   * @return true if equals
   */
  @Override
  public boolean equals(Object obj) {
    final Var other = Var.valueOf(obj);
    if (getType() == Var.Type.NULL || other.getType() == Var.Type.NULL) {
      return getType().equals(other.getType());
    }
    return this.toString().equals(other.toString());
  }

  public void inc(Object value) {
    Object result = null;

    switch (getType()) {
      case DATETIME: {
        getObjectAsDateTime().add(Calendar.DAY_OF_MONTH,  Var.valueOf(value).getObjectAsInt());
        break;
      }
      case INT: {
        result = getObjectAsLong() + Var.valueOf(value).getObjectAsLong();
        break;
      }
      default: {
        result = getObjectAsDouble() + Var.valueOf(value).getObjectAsDouble();
      }

    }

    if (result != null)
      setObject(result);
  }

  public void multiply(Object value) {
    Object result = null;

    switch (getType()) {
      case INT: {
        result = getObjectAsLong() * Var.valueOf(value).getObjectAsLong();
        break;
      }
      default: {
        result = getObjectAsDouble() * Var.valueOf(value).getObjectAsDouble();
      }

    }

    if (result != null)
      setObject(result);
  }

  public Var append(Object value) {
    Object result = getObjectAsString() + (value!= null?value.toString():"");
    setObject(result);
    return this;
  }

  /**
   * Check to see if this Var is less than some other var.
   *
   * @param var the var to compare to
   * @return true if it is less than
   */
  public boolean lessThan(Var var) {
    return this.compareTo(var) < 0;
  }

  /**
   * Check to see if this var is less than or equal to some other var
   *
   * @param var the var to compare to
   * @return true if this is less than or equal to var
   */
  public boolean lessThanOrEqual(Var var) {
    return this.compareTo(var) <= 0;
  }

  /**
   * Check to see if this var is greater than a given var.
   *
   * @param var the var to compare to.
   * @return true if this object is grater than the given var
   */
  public boolean greaterThan(Var var) {
    return this.compareTo(var) > 0;
  }

  /**
   * Check to see if this var is greater than or equal to a given var
   *
   * @param var the var to compare to
   * @return true if this var is greater than or equal to the given var
   */
  public boolean greaterThanOrEqual(Var var) {
    return this.compareTo(var) >= 0;
  }

  /**
   * Compare this object's value to another
   *
   * @param val the object to compare to
   * @return the value 0 if this is equal to the argument; a value less than 0
   * if this is numerically less than the argument; and a value greater than 0
   * if this is numerically greater than the argument (signed comparison).
   */
  @Override
  public int compareTo(Object val) {
    // only instantiate if val is not instance of Var
    Var var;
    if (val instanceof Var) {
      var = (Var) val;
    } else {
      var = new Var(val);
    }
    switch (getType()) {
      case STRING:
        return this.getObjectAsString().compareTo(var.getObjectAsString());
      case INT:
        if (var.getType().equals(Var.Type.INT)) {
          return ((Long) this.getObjectAsLong()).compareTo(var.getObjectAsLong());
        } else {
          return ((Double) this.getObjectAsDouble()).compareTo(var.getObjectAsDouble());
        }
      case DOUBLE:
        return ((Double) this.getObjectAsDouble()).compareTo(var.getObjectAsDouble());
      case BOOLEAN:
        return this.getObjectAsBoolean().compareTo(var.getObjectAsBoolean());
      case DATETIME:
        return this.getObjectAsDateTime().compareTo(var.getObjectAsDateTime());
      case LIST:
        // doesn't make sense
        return Integer.MAX_VALUE;
      default:
        // doesn't make sense
        return Integer.MAX_VALUE;
    }
  }

  /**
   * Convert this Var to a string format.
   *
   * @return the string format of this var
   */
  @Override
  public String toString() {
    switch (getType()) {
      case STRING:
        return getObject().toString();
      case INT:
        Long i = (Long) getObject();
        return i.toString();
      case DOUBLE:
        Double d = (double) _object;
        return _formatter.format(d);
      case DATETIME:
        return Utils.getDateFormat().format(((Calendar)getObject()).getTime());
      case LIST:
        LinkedList<Var> ll = (LinkedList) getObject();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Var v : ll) {
          if (first) {
            first = false;
            sb.append("{");
          } else {
            sb.append(", ");
          }
          sb.append(v.toString());
        }
        sb.append("}");
        return sb.toString();
      case NULL:
        return null;
      default:
        if (getObject() == null)
          return "";
        return getObject().toString();
    }
  }

  public Var negate() {
    if (getObjectAsBoolean()) {
      return VAR_FALSE;
    }

    return VAR_TRUE;
  }

  /**
   * Internal method for inferring the "object type" of this object. When it
   * is done, it sets the private member value of _type. This will be
   * referenced later on when various method calls are made on this object.
   */
  private void inferType() {
    if (_object == null) {
      _type = Type.NULL;
    } else if (_object instanceof Var) {
      Var oldObj = (Var) _object;
      _type = oldObj.getType();
      _object = oldObj.getObject();
      if (id == null)
        id = oldObj.id;
    } else if (_object instanceof String || _object instanceof StringBuilder || _object instanceof StringBuffer
        || _object instanceof Character) {
      _type = Type.STRING;
      _object = _object.toString();
    } else if (_object instanceof Boolean) {
      _type = Type.BOOLEAN;
    } else if (_object instanceof Date) {
      Date date = (Date) _object;
      _type = Type.DATETIME;
      _object = Calendar.getInstance();
      ((Calendar) _object).setTime(date);
    } else if (_object instanceof Calendar) {
      _type = Type.DATETIME;
    } else if (_object instanceof Long) {
      _type = Type.INT;
    } else if (_object instanceof Integer) {
      _type = Type.INT;
      _object = Long.valueOf((Integer)_object);
    } else if (_object instanceof Double) {
      _type = Type.DOUBLE;
    } else if (_object instanceof Float) {
      _type = Type.DOUBLE;
      _object = Double.valueOf((Float)_object);
    } else if (_object instanceof BigDecimal) {
      if (((BigDecimal) _object).scale() == 0) {
        _type = Type.INT;
        _object = ((BigDecimal) _object).longValue();
      } else {
        _type = Type.DOUBLE;
        _object = ((BigDecimal) _object).doubleValue();
      }
    } else if (_object instanceof BigInteger) {
      _type = Type.INT;
      _object = ((BigInteger) _object).longValue();
    } else if (_object instanceof LinkedList) {
      _type = Type.LIST;
    } else if (_object instanceof List) {
      _type = Type.LIST;
      _object = new LinkedList<>((List) _object);
    } else {
      _type = Type.UNKNOWN;
    }
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (id != null) {
      gen.writeStartObject();
      gen.writeObjectField(id, _object);
      gen.writeEndObject();
    } else {
      gen.writeObject(_object);
    }
  }

  @Override
  public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
    if (id != null) {
      gen.writeStartObject();
      gen.writeObjectField(id, _object);
      gen.writeEndObject();
    } else {
      gen.writeObject(_object);
    }
  }
}