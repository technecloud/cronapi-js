package cronapi.list;

import java.util.LinkedList;

import cronapi.Var;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-05-11
 *
 */

public class Operations {

	/**
	 * Construtor
	 **/

	public static final Var newList() throws Exception {
		return new Var(new LinkedList<Var>());
	}

	public static final Var isNull(Var list) throws Exception {
		if (list == Var.VAR_NULL)
			list = newList();
		return list;
	}

	public static final Var newList(Var... values) throws Exception {
		LinkedList<Var> linkedList = new LinkedList<Var>();
		for (Var v : values) {
			linkedList.add(v);
		}
		return new Var(linkedList);
	}

	public static final Var newListRepeat(Var item, Var times) throws Exception {
		LinkedList<Var> linkedList = new LinkedList<Var>();
		for (int i = 0; i < times.getObjectAsInt(); i++) {
			linkedList.add(item);
		}
		return new Var(linkedList);
	}

	public static final Var size(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST)
			return new Var(list.size());
		return Var.VAR_ZERO;
	}

	public static final Var isEmpty(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.size() > 0)
			return Var.VAR_FALSE;
		return Var.VAR_TRUE;
	}

	public static final Var findFirst(Var list, Var item) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().contains(item))
				return new Var(list.getObjectAsList().indexOf(item) + 1);
		}
		return Var.VAR_ZERO;
	}

	public static final Var findLast(Var list, Var item) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().contains(item))
				return new Var(list.getObjectAsList().lastIndexOf(item) + 1);
		}
		return Var.VAR_ZERO;
	}

	public static final Var get(Var list, Var index) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			if (index.getObjectAsInt() < 1)
				index = new Var(1);
			if (index.getObjectAsInt() > list.size())
				index = new Var(list.size());
			if (list.getObjectAsList().get(index.getObjectAsInt() - 1) != Var.VAR_NULL)
				return new Var(list.getObjectAsList().get(index.getObjectAsInt() - 1));
		}
		return Var.VAR_NULL;
	}

	public static final Var getFromEnd(Var list, Var index) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			Var i = new Var(list.getObjectAsList().size() - index.getObjectAsInt() + 1);
			Var item = get(list, i);
			if (item != Var.VAR_NULL)
				return new Var(item);
		}
		return Var.VAR_NULL;
	}

	public static final Var getFirst(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().getFirst());
		}
		return Var.VAR_NULL;
	}

	public static final Var getLast(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().getLast());
		}
		return Var.VAR_NULL;
	}

	public static final Var getRandom(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return cronapi.math.Operations.listRandomItem(list);
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemove(Var list, Var index) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			Var v = new Var(get(list, index));
			if (v != Var.VAR_NULL) {
				list.getObjectAsList().remove(v);
				return v;
			}
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveFromEnd(Var list, Var index) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			Var i = new Var(list.getObjectAsList().size() - index.getObjectAsInt());
			Var item = get(list, i);
			if (item != Var.VAR_NULL) {
				list.getObjectAsList().remove(item);
				return item;
			}
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveFirst(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			Var v = new Var(list.getObjectAsList().getFirst());
			list.getObjectAsList().removeFirst();
			return v;
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveLast(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			Var v = new Var(list.getObjectAsList().getLast());
			list.getObjectAsList().removeLast();
			return v;
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveRandom(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			Var v = cronapi.math.Operations.listRandomItem(list);
			list.getObjectAsList().remove(v);
			return v;
		}
		return Var.VAR_NULL;
	}

	public static final Var remove(Var list, Var index) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			Var item = get(list, index);
			if (item != Var.VAR_NULL)
				return new Var(list.getObjectAsList().remove(item));
		}
		return Var.VAR_FALSE;
	}

	public static final Var removeFromEnd(Var list, Var index) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			Var i = new Var(list.getObjectAsList().size() - index.getObjectAsInt());
			Var item = get(list, i);
			if (item != Var.VAR_NULL)
				return new Var(list.getObjectAsList().remove(item));
		}
		return Var.VAR_FALSE;
	}

	public static final Var removeFirst(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().removeFirst());
		}
		return Var.VAR_FALSE;
	}

	public static final Var removeLast(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().removeLast());
		}
		return Var.VAR_FALSE;
	}

	public static final Var removeRandom(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			int index = cronapi.math.Operations.randomInt(Var.VAR_ZERO, new Var(list.getObjectAsList().size() - 1))
					.getObjectAsInt();
			return new Var(list.getObjectAsList().remove(index));
		}
		return Var.VAR_FALSE;
	}

	// TODO
	public static final Var set(Var list, Var index, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			if (index.getObjectAsInt() < 1)
				index = new Var(1);
			if (list.size() > 0 && index.getObjectAsInt() > list.size()) {
				index = new Var(list.size());
				add(list, index, element);
			} else {
				list.getObjectAsList().set(index.getObjectAsInt() - 1, element);
			}
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var setFromEnd(Var list, Var index, Var element) throws Exception {
		list = isNull(list);
		if (index.getObjectAsInt() <= 1) {
			set(list, new Var(list.size()), element);
			return Var.VAR_TRUE;
		}

		if (index.getObjectAsInt() >= list.size()) {
			set(list, new Var(1), element);
			return Var.VAR_TRUE;
		}
		Var i = new Var(list.getObjectAsList().size() - index.getObjectAsInt() + 1);
		set(list, i, element);
		return Var.VAR_TRUE;
	}

	public static final Var setFirst(Var list, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
			set(list, Var.VAR_ZERO, element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var setLast(Var list, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
			set(list, new Var(list.getObjectAsList().size()), element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var setRandom(Var list, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
			int index = cronapi.math.Operations.randomInt(Var.VAR_ZERO, new Var(list.getObjectAsList().size() - 1))
					.getObjectAsInt();
			set(list, new Var(index), element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var add(Var list, Var index, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			if (index.getObjectAsInt() < 1)
				index = new Var(1);
			if (list.size() > 0 && index.getObjectAsInt() > list.size() + 1)
				index = new Var(list.size() + 1);
			list.getObjectAsList().add(index.getObjectAsInt() - 1, element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var addFromEnd(Var list, Var index, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() >= index.getObjectAsInt())
				&& (list.getObjectAsList().size() - index.getObjectAsInt()) >= 0 && index.getObjectAsInt() >= 0) {
			list.getObjectAsList().add(list.getObjectAsList().size() - index.getObjectAsInt(), element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var addFirst(Var list, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
			list.getObjectAsList().addFirst(element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var addLast(Var list, Var element) throws Exception {
		list = isNull(list);
		if (list == Var.VAR_NULL)
			list = new Var(new LinkedList<Var>());
		if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() >= 0)) {
			list.getObjectAsList().addLast(element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var addRandom(Var list, Var element) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && (list.getObjectAsList().size() > 0)) {
			int index = cronapi.math.Operations.randomInt(Var.VAR_ZERO, new Var(list.getObjectAsList().size() - 1))
					.getObjectAsInt();
			list.getObjectAsList().add(index, element);
			return Var.VAR_TRUE;
		}
		return Var.VAR_FALSE;
	}

	public static final Var getSublistFromNToN(Var list, Var indexInitial, Var indexFinal) throws Exception {
		list = isNull(list);
		int a = indexInitial.getObjectAsInt() - 1;
		int b = indexFinal.getObjectAsInt();
		if (list.getType() == Var.Type.LIST && get(list, new Var(a)) != Var.VAR_NULL
				&& get(list, new Var(b)) != Var.VAR_NULL) {
			Var result = new Var(list.getObjectAsList().subList(a, b));
			return result;
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromNToEnd(Var list, Var indexInitial, Var indexFinal) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && (indexInitial.getObjectAsInt() <= indexFinal.getObjectAsInt())
				&& (list.getObjectAsList().size() >= indexFinal.getObjectAsInt())) {
			Var result = new Var(
					list.getObjectAsList().subList(indexInitial.getObjectAsInt() - 1, indexFinal.getObjectAsInt()));
			return result;
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromNToLast(Var list, Var indexInitial) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.size() - indexInitial.getObjectAsInt() >= 0) {
			Var result = new Var(list.getObjectAsList().subList(indexInitial.getObjectAsInt() - 1, list.size()));
			return result;
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromEndToN(Var list, Var indexInitial, Var indexFinal) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.size() - indexInitial.getObjectAsInt() >= 0
				&& list.size() >= indexFinal.getObjectAsInt()) {
			return new Var(list.getObjectAsList().subList(list.size() - indexInitial.getObjectAsInt(),
					indexFinal.getObjectAsInt()));
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromEndToEnd(Var list, Var indexInitial, Var indexFinal) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.size() - indexInitial.getObjectAsInt() >= 0
				&& list.size() - (indexFinal.getObjectAsInt() + 1) >= 0 && indexInitial.getObjectAsInt() <= list.size()
				&& indexFinal.getObjectAsInt() <= list.size()) {
			return new Var(list.getObjectAsList().subList(list.size() - indexInitial.getObjectAsInt(),
					list.size() - indexFinal.getObjectAsInt() + 1));
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromEndToLast(Var list, Var indexInitial) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.size() - indexInitial.getObjectAsInt() >= 0
				&& indexInitial.getObjectAsInt() <= list.size()) {
			return new Var(list.getObjectAsList().subList(list.size() - indexInitial.getObjectAsInt(), list.size()));
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromFirstToN(Var list, Var indexInitial) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.size() - indexInitial.getObjectAsInt() >= 0
				&& indexInitial.getObjectAsInt() <= list.size()) {
			Var result = new Var(list.getObjectAsList().subList(0, indexInitial.getObjectAsInt()));
			return result;
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromFirstToEnd(Var list, Var indexInitial) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST && list.size() - indexInitial.getObjectAsInt() >= 0
				&& indexInitial.getObjectAsInt() >= 0 && indexInitial.getObjectAsInt() <= list.size()) {
			Var result = new Var(list.getObjectAsList().subList(0, list.size() - indexInitial.getObjectAsInt()));
			return result;
		}
		return Var.VAR_NULL;
	}

	public static final Var getSublistFromFirstToLast(Var list) throws Exception {
		list = isNull(list);
		if (list.getType() == Var.Type.LIST) {
			return list;
		}
		return Var.VAR_NULL;
	}

	public static final Var getTextFromList(Var list, Var limiter) throws Exception {
		list = isNull(list);
		Var result = new Var("");
		for (Var v : list.getObjectAsList()) {
			result.append(v.getObjectAsString());
			result.append(limiter.getObjectAsString());
		}

		return new Var(result.getObjectAsString().substring(0, result.getObjectAsString().length() - 1));
	}

	public static final Var getListFromText(Var text, Var limiter) throws Exception {
		text = isNull(text);
		LinkedList<Var> linked = new LinkedList<Var>();
		if (text.getType() == Var.Type.LIST) {
			for (Var v : text.getObjectAsList()) {
				if (!v.getObjectAsString().equals(limiter.getObjectAsString()))
					linked.add(v);
			}
			return new Var(linked);
		} else {
			String[] list = text.getObjectAsString().split(limiter.getObjectAsString());
			for (String s : list) {
				linked.addLast(new Var(s));
			}
			return new Var(linked);
		}
	}

	public static final Var orderListNumericGrowing(Var list) throws Exception {
		list = isNull(list);
		list.getObjectAsList().sort((p1, p2) -> p1.getObjectAsInt().compareTo(p2.getObjectAsInt()));

		return list;
	}

	public static final Var orderListNumericDecreasing(Var list) throws Exception {
		list = isNull(list);
		list.getObjectAsList().sort((p1, p2) -> p2.getObjectAsInt().compareTo(p1.getObjectAsInt()));

		return list;
	}

	public static final Var orderListAlphabeticGrowing(Var list) throws Exception {
		list = isNull(list);
		list.getObjectAsList().sort((p1, p2) -> p1.getObjectAsString().compareTo(p2.getObjectAsString()));
		return list;
	}

	public static final Var orderListAlphabeticDecreasing(Var list) throws Exception {
		list = isNull(list);
		list.getObjectAsList().sort((p1, p2) -> p2.getObjectAsString().compareTo(p1.getObjectAsString()));
		return list;
	}

	public static final Var orderListAlphabeticIgnoreCasesGrowing(Var list) throws Exception {
		list = isNull(list);
		list.getObjectAsList().sort((p1, p2) -> p1.getObjectAsString().compareToIgnoreCase(p2.getObjectAsString()));
		return list;
	}

	public static final Var orderListAlphabeticIgnoreCasesDecreasing(Var list) throws Exception {
		list = isNull(list);
		list.getObjectAsList().sort((p1, p2) -> p2.getObjectAsString().compareToIgnoreCase(p1.getObjectAsString()));
		return list;
	}

}
