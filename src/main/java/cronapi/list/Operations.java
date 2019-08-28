package cronapi.list;

import com.google.gson.JsonArray;
import cronapi.Var;
import cronapi.json.JsonArrayWrapper;
import java.util.LinkedList;
import java.util.List;

/**
 * Classe que representa ...
 *
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-05-11
 */

public class Operations {

  /**
   * Construtor
   **/

  public static final Var newList() {
    return Var.valueOf(new LinkedList<Var>());
  }

  private static Var ensureIsList(Var value) {
    if (value == Var.VAR_NULL) {
      return newList();
    } else if (value.getObject() instanceof JsonArray) {
      return Var.valueOf(new JsonArrayWrapper((JsonArray) value.getObject()));
    } else if (value.getObject() instanceof List) {
      return value;
    } else {
      return Var.valueOf(value.getObjectAsList());
    }
  }

  public static final Var newList(Var... values) throws Exception {
    LinkedList<Var> linkedList = new LinkedList<Var>();
    for (Var v : values) {
      linkedList.add(v);
    }
    return Var.valueOf(linkedList);
  }

  public static final Var newListRepeat(Var item, Var times) throws Exception {
    LinkedList<Var> linkedList = new LinkedList<Var>();
    for (int i = 0; i < times.getObjectAsInt(); i++) {
      linkedList.add(item);
    }
    return Var.valueOf(linkedList);
  }

  public static final Var size(Var list) {
    list = ensureIsList(list);
    return Var.valueOf(list.size());
  }

  public static final Var isEmpty(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && list.size() > 0) {
      return Var.VAR_FALSE;
    }
    return Var.VAR_TRUE;
  }

  public static final Var findFirst(Var list, Var item) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      if (list.getObjectAsList().contains(item)) {
        return Var.valueOf(list.getObjectAsList().indexOf(item) + 1);
      }
    }
    return Var.VAR_ZERO;
  }

  public static final Var findLast(Var list, Var item) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      if (list.getObjectAsList().contains(item)) {
        return Var.valueOf(list.getObjectAsList().lastIndexOf(item) + 1);
      }
    }
    return Var.VAR_ZERO;
  }

  public static final Var get(Var list, Var index) throws Exception {
    list = ensureIsList(list);
    int indexAsInt = index.getObjectAsInt();
    if (indexAsInt < 1) {
      indexAsInt = 1;
    }
    if (indexAsInt > list.size()) {
      indexAsInt = list.size();
    }
    if (list.getObjectAsList().get(indexAsInt - 1) != Var.VAR_NULL) {
      return Var.valueOf(list.getObjectAsList().get(index.getObjectAsInt() - 1));
    }
    return Var.VAR_NULL;
  }

  public static final Var getFromEnd(Var list, Var index) throws Exception {
    list = ensureIsList(list);
    int i = list.getObjectAsList().size() - index.getObjectAsInt() + 1;
    Var item = get(list, Var.valueOf(i));
    if (item != Var.VAR_NULL) {
      return Var.valueOf(item);
    }
    return Var.VAR_NULL;
  }

  public static final Var getFirst(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getObjectAsList().size() > 0) {
      return Var.valueOf(list.getObjectAsList().get(0));
    }
    return Var.VAR_NULL;
  }

  public static final Var getLast(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getObjectAsList().size() > 0) {
      return Var.valueOf(list.getObjectAsList().get(list.size() - 1));
    }
    return Var.VAR_NULL;
  }

  public static final Var getRandom(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getObjectAsList().size() > 0) {
      return cronapi.math.Operations.listRandomItem(list);
    }
    return Var.VAR_NULL;
  }

  public static final Var getAndRemove(Var list, Var index) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      Var v = Var.valueOf(get(list, index));
      if (v != Var.VAR_NULL) {
        list.getObjectAsList().remove(v);
        return v;
      }
    }
    return Var.VAR_NULL;
  }

  public static final Var getAndRemoveFromEnd(Var list, Var index) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      Var i = Var.valueOf(list.getObjectAsList().size() - index.getObjectAsInt());
      Var item = get(list, i);
      if (item != Var.VAR_NULL) {
        list.getObjectAsList().remove(item);
        return item;
      }
    }
    return Var.VAR_NULL;
  }

  public static final Var getAndRemoveFirst(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
      Var v = Var.valueOf(list.getObjectAsList().get(0));
      list.getObjectAsList().remove(0);
      return v;
    }
    return Var.VAR_NULL;
  }

  public static final Var getAndRemoveLast(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
      Var v = Var.valueOf(list.getObjectAsList().get(list.size() - 1));
      list.getObjectAsList().remove(list.size() - 1);
      return v;
    }
    return Var.VAR_NULL;
  }

  public static final Var getAndRemoveRandom(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
      Var v = cronapi.math.Operations.listRandomItem(list);
      list.getObjectAsList().remove(v);
      return v;
    }
    return Var.VAR_NULL;
  }

  public static final Var remove(Var list, Var index) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      Var item = get(list, index);
      if (item != Var.VAR_NULL) {
        return Var.valueOf(list.getObjectAsList().remove(item));
      }
    }
    return Var.VAR_FALSE;
  }

  public static final Var removeFromEnd(Var list, Var index) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      Var i = Var.valueOf(list.getObjectAsList().size() - index.getObjectAsInt());
      Var item = get(list, i);
      if (item != Var.VAR_NULL) {
        return Var.valueOf(list.getObjectAsList().remove(item));
      }
    }
    return Var.VAR_FALSE;
  }

  public static final Var removeFirst(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
      return Var.valueOf(list.getObjectAsList().remove(0));
    }
    return Var.VAR_FALSE;
  }

  public static final Var removeLast(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
      return Var.valueOf(list.getObjectAsList().remove(list.size() - 1));
    }
    return Var.VAR_FALSE;
  }

  public static final Var removeRandom(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
      int index = cronapi.math.Operations
          .randomInt(Var.VAR_ZERO, Var.valueOf(list.getObjectAsList().size() - 1))
          .getObjectAsInt();
      return Var.valueOf(list.getObjectAsList().remove(index));
    }
    return Var.VAR_FALSE;
  }

  // TODO
  public static final Var set(Var list, Var index, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      if (index.getObjectAsInt() < 1) {
        index = Var.valueOf(1);
      }
      if (list.size() > 0 && index.getObjectAsInt() > list.size()) {
        index = Var.valueOf(list.size());
        add(list, index, element);
      } else {
        list.getObjectAsList().set(index.getObjectAsInt() - 1, element);
      }
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var setFromEnd(Var list, Var index, Var element) throws Exception {
    list = ensureIsList(list);
    if (index.getObjectAsInt() <= 1) {
      set(list, Var.valueOf(list.size()), element);
      return Var.VAR_TRUE;
    }

    if (index.getObjectAsInt() >= list.size()) {
      set(list, Var.valueOf(1), element);
      return Var.VAR_TRUE;
    }
    Var i = Var.valueOf(list.getObjectAsList().size() - index.getObjectAsInt() + 1);
    set(list, i, element);
    return Var.VAR_TRUE;
  }

  public static final Var setFirst(Var list, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
      set(list, Var.VAR_ZERO, element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var setLast(Var list, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
      set(list, Var.valueOf(list.getObjectAsList().size()), element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var setRandom(Var list, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
      int index = cronapi.math.Operations
          .randomInt(Var.VAR_ZERO, Var.valueOf(list.getObjectAsList().size() - 1))
          .getObjectAsInt();
      set(list, Var.valueOf(index), element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var add(Var list, Var index, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      if (index.getObjectAsInt() < 1) {
        index = Var.valueOf(1);
      }
      if (list.size() > 0 && index.getObjectAsInt() > list.size() + 1) {
        index = Var.valueOf(list.size() + 1);
      }
      list.getObjectAsList().add(index.getObjectAsInt() - 1, element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var addFromEnd(Var list, Var index, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() >= index.getObjectAsInt())
        &&
        (list.getObjectAsList().size() - index.getObjectAsInt()) >= 0
        && index.getObjectAsInt() >= 0) {
      list.getObjectAsList().add(list.getObjectAsList().size() - index.getObjectAsInt(), element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var addFirst(Var list, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
      list.getObjectAsList().add(0,element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var addLast(Var list, Var element) throws Exception {
    list = ensureIsList(list);
    if (list == Var.VAR_NULL) {
      list = Var.valueOf(new LinkedList<Var>());
    }
    if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() >= 0)) {
      list.getObjectAsList().add(element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var addRandom(Var list, Var element) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
      int index = cronapi.math.Operations
          .randomInt(Var.VAR_ZERO, Var.valueOf(list.getObjectAsList().size() - 1))
          .getObjectAsInt();
      list.getObjectAsList().add(index, element);
      return Var.VAR_TRUE;
    }
    return Var.VAR_FALSE;
  }

  public static final Var getSublistFromNToN(Var list, Var indexInitial, Var indexFinal)
      throws Exception {
    list = ensureIsList(list);
    int start = getStartIndex(list, indexInitial).getObjectAsInt();
    int end = getEndIndex(list, indexFinal).getObjectAsInt();
    if (list.getType() == Var.Type.LIST) {
      Var result = Var.valueOf(list.getObjectAsList().subList(start, end));
      return result;
    }
    return Var.newList();
  }

  public static final Var getSublistFromNToEnd(Var list, Var indexInitial, Var indexFinal)
      throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      int start = getStartIndex(list, indexInitial).getObjectAsInt();
      int end = getEndIndex(list, Var.valueOf(indexFinal.getObjectAsInt() - 1)).getObjectAsInt();
      Var result = Var.valueOf(
          list.getObjectAsList().subList(start, list.getObjectAsList().size() - end));
      return result;
    }
    return Var.newList();
  }

  public static final Var getSublistFromNToLast(Var list, Var indexInitial) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      int start = getStartIndex(list, indexInitial).getObjectAsInt();
      Var result = Var.valueOf(list.getObjectAsList().subList(start, list.size()));
      return result;
    }
    return Var.newList();
  }

  public static final Var getSublistFromEndToN(Var list, Var indexInitial, Var indexFinal)
      throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      int start = getStartIndex(list, indexInitial).getObjectAsInt();
      int end = getEndIndex(list, indexFinal).getObjectAsInt();
      return Var.valueOf(list.getObjectAsList().subList(list.size() - start, end));
    }
    return Var.newList();
  }

  public static final Var getSublistFromEndToEnd(Var list, Var indexInitial, Var indexFinal)
      throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      int start = getStartIndex(list,
          Var.valueOf((list.size() - indexInitial.getObjectAsInt()) + 1)).getObjectAsInt();
      int end = getEndIndex(list, Var.valueOf((list.size() - indexFinal.getObjectAsInt()) + 1))
          .getObjectAsInt();
      return Var.valueOf(list.getObjectAsList().subList(start, end));
    }
    return Var.newList();
  }

  public static final Var getSublistFromEndToLast(Var list, Var indexInitial) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      int start = getStartIndex(list, indexInitial).getObjectAsInt() + 1;
      return Var.valueOf(list.getObjectAsList().subList(list.size() - start, list.size()));
    }
    return Var.newList();
  }

  public static final Var getSublistFromFirstToN(Var list, Var indexFinal) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      int end = getEndIndex(list, indexFinal).getObjectAsInt();
      Var result = Var.valueOf(list.getObjectAsList().subList(0, end));
      return result;
    }
    return Var.newList();
  }

  public static final Var getSublistFromFirstToEnd(Var list, Var indexFinal) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      int end = getEndIndex(list, Var.valueOf(indexFinal.getObjectAsInt() - 1)).getObjectAsInt();
      Var result = Var.valueOf(list.getObjectAsList().subList(0, list.size() - end));
      return result;
    }
    return Var.newList();
  }

  public static final Var getSublistFromFirstToLast(Var list) throws Exception {
    list = ensureIsList(list);
    if (list.getType() == Var.Type.LIST) {
      return list;
    }
    return Var.newList();
  }

  public static final Var getTextFromList(Var list, Var limiter) throws Exception {
    list = ensureIsList(list);
    Var result = Var.valueOf("");
    for (Object v : list.getObjectAsList()) {
      result.append(Var.valueOf(v).getObjectAsString());
      result.append(limiter.getObjectAsString());
    }

    return Var.valueOf(
        result.getObjectAsString().substring(0, result.getObjectAsString().length() - 1));
  }

  public static final Var getListFromText(Var text, Var limiter) {
    List<Var> linked = new LinkedList<>();
    if (text.getType() == Var.Type.LIST) {
      for (Object v : text.getObjectAsList()) {
        if (!Var.valueOf(v).getObjectAsString().equals(limiter.getObjectAsString())) {
          linked.add(Var.valueOf(v));
        }
      }
    } else {
      String[] list = text.getObjectAsString().split(limiter.getObjectAsString());
      for (String s : list) {
        linked.add(Var.valueOf(s));
      }
    }
    return Var.valueOf(linked);
  }

  public static final Var orderListNumericGrowing(Var list) throws Exception {
    try {
      list = ensureIsList(list);
      list.getObjectAsList().sort((p1, p2) -> Var.valueOf(p1).getObjectAsInt().compareTo(Var.valueOf(p2).getObjectAsInt()));
      return list;
    } catch (Exception e) {
      throw e;
    }
  }

  public static final Var orderListNumericDecreasing(Var list) throws Exception {
    list = ensureIsList(list);
    list.getObjectAsList().sort((p1, p2) -> Var.valueOf(p2).getObjectAsInt().compareTo(Var.valueOf(p1).getObjectAsInt()));

    return list;
  }

  public static final Var orderListAlphabeticGrowing(Var list) throws Exception {
    list = ensureIsList(list);
    list.getObjectAsList()
        .sort((p1, p2) -> Var.valueOf(p1).getObjectAsString().compareTo(Var.valueOf(p2).getObjectAsString()));
    return list;
  }

  public static final Var orderListAlphabeticDecreasing(Var list) throws Exception {
    list = ensureIsList(list);
    list.getObjectAsList()
        .sort((p1, p2) -> Var.valueOf(p2).getObjectAsString().compareTo(Var.valueOf(p1).getObjectAsString()));
    return list;
  }

  public static final Var orderListAlphabeticIgnoreCasesGrowing(Var list) throws Exception {
    list = ensureIsList(list);
    list.getObjectAsList()
        .sort((p1, p2) -> Var.valueOf(p1).getObjectAsString().compareToIgnoreCase(Var.valueOf(p2).getObjectAsString()));
    return list;
  }

  public static final Var orderListAlphabeticIgnoreCasesDecreasing(Var list) throws Exception {
    list = ensureIsList(list);
    list.getObjectAsList()
        .sort((p1, p2) -> Var.valueOf(p2).getObjectAsString().compareToIgnoreCase(Var.valueOf(p1).getObjectAsString()));
    return list;
  }

  private static final Var getStartIndex(Var list, Var index) {
    list = ensureIsList(list);
    if (index.getObjectAsInt() < 0) {
      return Var.valueOf(0);
    }
    if (index.getObjectAsInt() > list.getObjectAsList().size()) {
      return Var.valueOf(list.getObjectAsList().size());
    }
    return Var.valueOf(index.getObjectAsInt() - 1);
  }

  private static final Var getEndIndex(Var list, Var index) {
    list = ensureIsList(list);
    if (index.getObjectAsInt() < 0) {
      return Var.valueOf(0);
    }
    if (index.getObjectAsInt() > list.getObjectAsList().size()) {
      return Var.valueOf(list.getObjectAsList().size());
    }
    return Var.valueOf(index.getObjectAsInt());
  }

}
