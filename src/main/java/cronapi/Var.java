package cronapi;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Var implements Comparable {

	public enum Type {
		STRING, INT, DOUBLE, LIST, NULL, UNKNOWN, BOOLEAN, LONG
	};

	private String id;
	private Type _type;
	private Object _object;
	private static final NumberFormat _formatter = new DecimalFormat("#.#####");

	public static final Var VAR_NULL = new Var(null);
	public static final Var VAR_TRUE = new Var(true);
	public static final Var VAR_FALSE = new Var(false);
	public static final Var VAR_ZERO = new Var(0);
	public static final Var VAR_NEGATIVE_ONE = new Var(-1);

	/**
	 * Construct a Var with an NULL type
	 *
	 */
	public Var() {
		_type = Type.NULL;
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

	/**
	 * Construct a Var from a given Var
	 *
	 * @param var var to construct this one from
	 */
	public Var(Var var) {
		_type = Type.UNKNOWN;
		if (var!=null)
			setObject(var.getObject());
	}

	/**
	 * Set the value of the underlying object. Note that the type of Var will be
	 * determined when setObject is called.
	 *
	 * @param val the value to set this Var to
	 */
	private void setObject(Object val) {
		this._object = val;
		inferType();
		// make sure each element of List is Var if type is list
		if (_type.equals(Var.Type.LIST)) {
			LinkedList<Var> myList = new LinkedList<>();
			for (Object obj : this.getObjectAsList()) {
				myList.add(new Var(obj));
			}
			this._object = myList;
		}
	}

	/**
	 * Static constructor to make a var from some value.
	 *
	 * @param val some value to construct a var around
	 * @return the Var object
	 */
	public static Var valueOf(Object val) {
		if (val instanceof  Var)
			return (Var) val;

		if (val instanceof Boolean) {
			if (((Boolean) val)) {
				return VAR_TRUE;
			} else {
				return VAR_FALSE;
			}
		}
		return new Var(val);
	}

	public static Var valueOf(String id, Object val) {
		if (val instanceof  Var && Objects.equals(((Var) val).getId(), id))
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
	public int getObjectAsInt() {
		switch (getType()) {
			case STRING:
				return Integer.parseInt((String) getObject());
			case INT:
				return (int) getObject();
			case BOOLEAN:
				return ((boolean) getObject()) ? 1 : 0;
			case DOUBLE:
				return new Double((double) getObject()).intValue();
			case LONG:
				return new Long((long) getObject()).intValue();
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
	 * Get object as an boolean.
	 *
	 * @return an bool whose value equals this object
	 */
	public boolean getObjectAsBoolean() {
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
				return (int) getObject() > 0;
			case BOOLEAN:
				return (boolean) getObject();
			case DOUBLE:
				return new Double((double) getObject()).intValue() > 0;
			case LONG:
				return new Long((long) getObject()).intValue() > 0;
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
	public double getObjectAsDouble() {
		switch (getType()) {
			case STRING:
				return Double.parseDouble((String) getObject());
			case INT:
				return new Integer((int) getObject()).doubleValue();
			case BOOLEAN:
				return ((boolean) getObject()) ? 1.0 : 0.0;
			case DOUBLE:
				return (double) getObject();
			case LONG:
				return new Long((long) getObject()).doubleValue();
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
	 * Get object as a long. Does not make sense for a "LIST" type object.
	 *
	 * @return a long whose value equals this object
	 */
	public long getObjectAsLong() {
		switch (getType()) {
			case STRING:
				return Long.parseLong((String) getObject());
			case INT:
				return new Integer((int) getObject()).longValue();
			case BOOLEAN:
				return ((boolean) getObject()) ? 1 : 0;
			case DOUBLE:
				return new Double((double) getObject()).longValue();
			case LONG:
				return (long) getObject();
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
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		final Var other = Var.valueOf(obj);
		if (getType() == Var.Type.NULL || other.getType() == Var.Type.NULL) {
			return getType().equals(other.getType());
		}
		return this.toString().equals(other.toString());
	}

	/**
	 * Check to see if this Var is less than some other var.
	 *
	 * @param var the var to compare to
	 * @return true if it is less than
	 */
	public boolean lessThan(Var var) {
		switch (getType()) {
			case STRING:
				return this.getObjectAsString().compareTo(var.getObjectAsString()) < 0;
			case INT:
				return this.getObjectAsInt() < var.getObjectAsDouble();
			case DOUBLE:
				return this.getObjectAsDouble() < var.getObjectAsDouble();
			case LIST:
				if (size() != var.size()) {
					return false;
				}
				if (!var.getType().equals(Var.Type.LIST)) {
					return false;
				}
				int index = 0;
				for (Var myVar : this.getObjectAsList()) {
					if (!myVar.lessThan(var.get(index))) {
						return false;
					}
				}
				return true;
			default:
				return false;
		}
	}

	/**
	 * Check to see if this var is less than or equal to some other var
	 *
	 * @param var the var to compare to
	 * @return true if this is less than or equal to var
	 */
	public boolean lessThanOrEqual(Var var) {
		switch (getType()) {
			case STRING:
				return this.getObjectAsString().compareTo(var.getObjectAsString()) <= 0;
			case INT:
				return this.getObjectAsInt() <= var.getObjectAsDouble();
			case DOUBLE:
				return this.getObjectAsDouble() <= var.getObjectAsDouble();
			case LIST:
				if (size() != var.size()) {
					return false;
				}
				if (!var.getType().equals(Var.Type.LIST)) {
					return false;
				}
				int index = 0;
				for (Var myVar : this.getObjectAsList()) {
					if (!myVar.lessThanOrEqual(var.get(index))) {
						return false;
					}
				}
				return true;
			case NULL:
				return (var.getType() == Var.Type.NULL);
			default:
				return false;
		}
	}

	/**
	 * Check to see if this var is greater than a given var.
	 *
	 * @param var the var to compare to.
	 * @return true if this object is grater than the given var
	 */
	public boolean greaterThan(Var var) {
		return var.lessThan(this);
	}

	/**
	 * Check to see if this var is greater than or equal to a given var
	 *
	 * @param var the var to compare to
	 * @return true if this var is greater than or equal to the given var
	 */
	public boolean greaterThanOrEqual(Var var) {
		return var.lessThanOrEqual(this);
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
					return ((Integer) this.getObjectAsInt()).compareTo(var.getObjectAsInt());
				} else {
					return ((Double) this.getObjectAsDouble()).compareTo(var.getObjectAsDouble());
				}
			case DOUBLE:
				return ((Double) this.getObjectAsDouble()).compareTo(var.getObjectAsDouble());
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
				Integer i = (int) getObject();
				return i.toString();
			case DOUBLE:
				Double d = (double) _object;
				return _formatter.format(d);
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
		} else if (_object instanceof String) {
			_type = Type.STRING;
		} else {
			try {
				Boolean b = (boolean) _object;
				_type = Type.BOOLEAN;
			} catch (Exception nBo) {
				// must be a number or a list
				// try to see if its a double
				try {
					Double d = (double) _object;
					// it was a double, so keep going
					_type = Type.DOUBLE;
				} catch (Exception ex) {
					try {
						Long d = (long) _object;
						_type = Type.LONG;
					} catch (Exception exL) {
						// not a double, see if it is an integer
						try {
							Integer i = (int) _object;
							// it was an integer
							_type = Type.INT;
						} catch (Exception ex2) {
							// not a double or integer, might be an array
							if (_object instanceof LinkedList) {
								_type = Type.LIST;
							} else if (_object instanceof List) {
								_type = Type.LIST;
								_object = new LinkedList<>((List) _object);
							} else {
								_type = Type.UNKNOWN;
							}
						}
					}

				}
			}
		}
	}
}