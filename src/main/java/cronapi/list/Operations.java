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
		if (list.getType() == Var.Type.LIST)
			return new Var(list.size());
		return new Var(0);
	}

	public static final Var isEmpty(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.size() > 0)
			return new Var(false);
		return new Var(true);
	}

	public static final Var findFirst(Var list, Var item) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().contains(item))
				return new Var(list.getObjectAsList().get(list.getObjectAsList().indexOf(item)));
		}
		return Var.VAR_NULL;
	}

	public static final Var findLast(Var list, Var item) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().contains(item))
				return new Var(list.getObjectAsList().get(list.getObjectAsList().lastIndexOf(item)));
		}
		return Var.VAR_NULL;
	}

	public static final Var get(Var list, Var index) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().get(index.getObjectAsInt() - 1) != Var.VAR_NULL)
				return new Var(list.getObjectAsList().get(index.getObjectAsInt() - 1));
		}
		return Var.VAR_NULL;
	}

	public static final Var getFromEnd(Var list, Var index) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().get(list.getObjectAsList().size() - index.getObjectAsInt()) != Var.VAR_NULL)
				return new Var(list.getObjectAsList().get(list.getObjectAsList().size() - index.getObjectAsInt()));
		}
		return Var.VAR_NULL;
	}

	public static final Var getFirst(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().getFirst());
		}
		return Var.VAR_NULL;
	}

	public static final Var getLast(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().getLast());
		}
		return Var.VAR_NULL;
	}

	public static final Var getRandom(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return cronapi.math.Operations.listRandomItem(list);
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemove(Var list, Var index) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().get(index.getObjectAsInt() - 1) != Var.VAR_NULL) {
				Var v = new Var(list.getObjectAsList().get(index.getObjectAsInt() - 1));
				list.getObjectAsList().remove(index.getObjectAsInt() - 1);
				return v;
			}
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveFromEnd(Var list, Var index) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().get(list.getObjectAsList().size() - index.getObjectAsInt()) != Var.VAR_NULL) {
				Var v = new Var(list.getObjectAsList().get(list.getObjectAsList().size() - index.getObjectAsInt()));
				list.getObjectAsList().remove(list.getObjectAsList().size() - index.getObjectAsInt());
				return v;
			}
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveFirst(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			Var v = new Var(list.getObjectAsList().getFirst());
			list.getObjectAsList().removeFirst();
			return v;
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveLast(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			Var v = new Var(list.getObjectAsList().getLast());
			list.getObjectAsList().removeLast();
			return v;
		}
		return Var.VAR_NULL;
	}

	public static final Var getAndRemoveRandom(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			Var v = cronapi.math.Operations.listRandomItem(list);
			list.getObjectAsList().remove(v);
			return v;
		}
		return Var.VAR_NULL;
	}

	public static final Var remove(Var list, Var index) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().get(index.getObjectAsInt() - 1) != Var.VAR_NULL)
				return new Var(list.getObjectAsList().remove(index.getObjectAsInt() - 1));
		}
		return Var.VAR_NULL;
	}

	public static final Var removeFromEnd(Var list, Var index) throws Exception {
		if (list.getType() == Var.Type.LIST) {
			if (list.getObjectAsList().get(list.getObjectAsList().size() - index.getObjectAsInt()) != Var.VAR_NULL)
				return new Var(list.getObjectAsList().remove(list.getObjectAsList().size() - index.getObjectAsInt()));
		}
		return Var.VAR_NULL;
	}

	public static final Var removeFirst(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().removeFirst());
		}
		return Var.VAR_NULL;
	}

	public static final Var removeLast(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
			return new Var(list.getObjectAsList().removeLast());
		}
		return Var.VAR_NULL;
	}

	public static final Var removeRandom(Var list) throws Exception {
		if (list.getType() == Var.Type.LIST && list.getObjectAsList().size() > 0) {
      int index = cronapi.math.Operations.randomInt(Var.VAR_ZERO, new Var(list.getObjectAsList().size()-1)).getObjectAsInt();
			list.getObjectAsList().remove(index);
			return new Var(true);
		}
		return Var.VAR_NULL;
	}

	public static void main(String[] args) {
		LinkedList<Var> linked = new LinkedList<Var>();
		linked.add(new Var(1));
		linked.add(new Var(10));
		linked.add(new Var(11));
		Var list = new Var(linked);
		try {

			System.out.println("Função:" + Operations.removeRandom(list));
			System.out.println(list);

		} catch (Exception e) {
			System.out.println("Erro" + e);
		}
	}

}
