package cronapi.text;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;

import java.text.Normalizer;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-03
 *
 */
@CronapiMetaData(category = CronapiMetaData.CategoryType.TEXT, categoryTags = { "Text", "Texto" })
public class Operations {

	public static final Var newText(Var text) throws Exception {
		return new Var(text.getObjectAsString());
	}

	public static final Var newText(Var... text) throws Exception {
		Var result = new Var("");
		for (Var t : text) {
			result.append(t.getObjectAsString());
		}
		return result;
	}

	public static final Var concat(Var item, Var... itens) throws Exception {
		for (Var t : itens) {
			item.append(t.getObjectAsString());
		}
		return item;
	}

	public static final Var titleCase(Var text) {
		StringBuilder titleCase = new StringBuilder();
		boolean nextTitleCase = true;
		String input = text.getObjectAsString();

		for (char c : input.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				nextTitleCase = true;
			} else if (nextTitleCase) {
				c = Character.toTitleCase(c);
				nextTitleCase = false;
			}
			titleCase.append(c);
		}
		return Var.valueOf(titleCase.toString());
	}

	public static final Var getLetter(Var text, Var index) throws Exception {
		if (text.getType().equals(Var.Type.NULL))
			return Var.VAR_NULL;
		return (text.getObjectAsString().length() >= index.getObjectAsInt())
				? (index.getObjectAsInt() < 1 ? new Var(text.getObjectAsString().charAt(0))
						: new Var(text.getObjectAsString().charAt(index.getObjectAsInt() - 1)))
				: new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - 1));
	}

	public static final Var getLetterFromEnd(Var text, Var index) throws Exception {
		if (text.getType().equals(Var.Type.NULL))
			return Var.VAR_NULL;
		if (index.getObjectAsInt() <= 0)
			return new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - 1));
		return (text.getObjectAsString().length() - index.getObjectAsInt() > 0)
				? new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - index.getObjectAsInt()))
				: new Var(text.getObjectAsString().charAt(0));
	}

	public static final Var getFirstLetter(Var text) throws Exception {
		return getLetter(text, Var.valueOf(1));
	}

	public static final Var getLastLetter(Var text) throws Exception {
		return getLetter(text, Var.valueOf(text.getObjectAsString().length()));
	}

	public static final Var getRandomLetter(Var text) throws Exception {
		int i = new java.util.Random().nextInt(text.getObjectAsString().length());
		if (i == 0)
			i++;
		return getLetter(text, Var.valueOf(i));
	}

	public static final Var getLettersFromStartToFromStart(Var text, Var index1, Var index2) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index2.getObjectAsInt() < 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());
		if (index2.getObjectAsInt() > text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());

		if (index1.getObjectAsInt() <= index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring(index1.getObjectAsInt() - 1, index2.getObjectAsInt()));
		} else {
			return new Var(text.getObjectAsString().substring(index2.getObjectAsInt() - 1, index1.getObjectAsInt()));
		}
	}

	public static final Var getLettersFromStartToFromEnd(Var text, Var index1, Var index2) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index2.getObjectAsInt() <= 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());
		if (index2.getObjectAsInt() > text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());

		if (index1.getObjectAsInt() <= (text.getObjectAsString().length() - index2.getObjectAsInt())) {
			return new Var(text.getObjectAsString().substring(index1.getObjectAsInt() - 1,
					text.getObjectAsString().length() - (index2.getObjectAsInt() - 1)));
		}
		if (index1.getObjectAsInt() == 1 && index2.getObjectAsInt() == text.getObjectAsString().length())
			return new Var(text.getObjectAsString().substring(0, 1));

		return Var.VAR_NULL;
	}

	public static final Var getLettersFromStartToLast(Var text, Var index1) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length() + 1);
		return new Var(text.getObjectAsString().substring(index1.getObjectAsInt() - 1));
	}

	public static final Var getLettersFromEndToFromStart(Var text, Var index1, Var index2) throws Exception {
		if (index2.getObjectAsInt() < 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);

		if (index2.getObjectAsInt() >= text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());

		if (index2.getObjectAsInt() == 1 && (text.length() - index1.getObjectAsInt()) == 0) {
			return new Var(text.getObjectAsString().substring(0, 1));
		} else if ((text.length() - index1.getObjectAsInt()) < index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring((text.length() - (index1.getObjectAsInt())),
					index2.getObjectAsInt()));
		}
		return Var.VAR_NULL;
	}

	public static final Var getLettersFromEndToFromEnd(Var text, Var index1, Var index2) throws Exception {
		if (index2.getObjectAsInt() < 1)
			index2 = new Var(1);
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);

		if (index2.getObjectAsInt() > text.getObjectAsString().length())
			index2 = new Var(text.getObjectAsString().length());
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());

		if (index1.getObjectAsInt() > index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring((text.length() - index1.getObjectAsInt()),
					(text.length() - index2.getObjectAsInt() + 1)));
		} else if (index1.getObjectAsInt() == index2.getObjectAsInt()) {
			return new Var(text.getObjectAsString().substring((text.length() - (index1.getObjectAsInt())),
					(text.length() - (index1.getObjectAsInt() - 1))));
		}
		return Var.VAR_NULL;
	}

	public static final Var getLettersFromEndToFromLast(Var text, Var index1) throws Exception {
		if (text.getType().equals(Var.Type.NULL) || text.getObjectAsString().length() < 1) {
			return Var.VAR_NULL;
		}
		if (index1.getObjectAsInt() < 1) {
			return new Var(text.getObjectAsString().charAt(text.getObjectAsString().length() - 1));
		} else if (index1.getObjectAsInt() > text.getObjectAsString().length()) {
			index1 = new Var(text.getObjectAsString().length() + 1);
			return text;
		} else {
			return new Var(text.getObjectAsString().substring((text.length() - (index1.getObjectAsInt()))));
		}
	}

	public static final Var getLettersFromFirstToFromStart(Var text, Var index1) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			index1 = new Var(text.getObjectAsString().length());
		return new Var(text.getObjectAsString().substring(0, index1.getObjectAsInt()));
	}

	public static final Var getLettersFromFirstToFromEnd(Var text, Var index1) throws Exception {
		if (index1.getObjectAsInt() < 1)
			index1 = new Var(1);
		if (index1.getObjectAsInt() > text.getObjectAsString().length())
			return new Var(text.getObjectAsString().substring(0, 1));
		return new Var(text.getObjectAsString().substring(0, text.length() - (index1.getObjectAsInt() - 1)));
	}

	public static final Var getLettersFromFirstToEnd(Var text) throws Exception {
		return new Var(text.getObjectAsString());
	}

	@CronapiMetaData(type = "function", name = "{{newline}}", nameTags = {
			"newline", "nova linha" }, description = "{{newlineDescription}}", returnType = ObjectType.STRING)
	public static final Var newline() {
		return Var.valueOf("\n");
	}

	@CronapiMetaData(type = "function", name = "{{textReplaceFunction}}", nameTags = {
			"replace", "substituir" }, description = "{{textReplaceDescription}}", returnType = ObjectType.STRING)
	public static final Var replace(
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplace}}", defaultValue = "Site is crxnapp.ix") Var textReplace,
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceTarget}}", defaultValue = "x") Var target,
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceReplacement}}", defaultValue = "o") Var replacement) {
		if (validationReplace(textReplace, target, replacement)) return Var.VAR_NULL;
		return Var.valueOf(textReplace.getObjectAsString().replace(target.getObjectAsString(), replacement.getObjectAsString()).trim());
	}

	@CronapiMetaData(type = "function", name = "{{textReplaceAllFunction}}", nameTags = {
			"replace", "substituir" }, description = "{{textReplaceAllDescription}}", returnType = ObjectType.STRING)
	public static final Var replaceAll(
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplace}}", defaultValue = "My .com site is cronapp.com") Var textReplace,
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceRegex}}", defaultValue = "com") Var regex,
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceReplacement}}", defaultValue = "io") Var replacement) {
		if (validationReplace(textReplace, regex, replacement)) return Var.VAR_NULL;
		return Var.valueOf(textReplace.getObjectAsString().replaceAll(regex.getObjectAsString(), replacement.getObjectAsString()).trim());
	}

	@CronapiMetaData(type = "function", name = "{{textReplaceFirstFunction}}", nameTags = {
			"replace", "substituir" }, description = "{{textReplaceFirstDescription}}", returnType = ObjectType.STRING)
	public static final Var replaceFirst(
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplace}}", defaultValue = "Site is Test.com") Var textReplace,
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceRegex}}", defaultValue = "Test(.*)") Var regex,
			@ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceReplacement}}", defaultValue = "cronapp.io") Var replacement) {
		if (validationReplace(textReplace, regex, replacement)) return Var.VAR_NULL;
		return Var.valueOf(textReplace.getObjectAsString().replaceFirst(regex.getObjectAsString(), replacement.getObjectAsString()).trim());
	}

	@CronapiMetaData(name = "{{textNormalizeFunction}}", nameTags = {
			"normalize", "normalizar" }, description = "{{textNormalizeDescription}}", returnType = ObjectType.STRING)
	public static final Var normalize(
			@ParamMetaData(type = ObjectType.STRING, description = "{{nonNormalizedText}}") Var nonNormalizedText) {
		String normalizedText = Normalizer
				.normalize(nonNormalizedText.getObjectAsString(), Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
		return Var.valueOf(normalizedText);
	}

	@CronapiMetaData(name = "{{textStartsWithFunction}}", nameTags = {
			"startsWith", "começa com" }, description = "{{textStartsWithDescription}}", returnType = ObjectType.BOOLEAN)
	public static final Var startsWith(
			@ParamMetaData(type = ObjectType.STRING, description = "{{textStarts}}") Var textStarts,
			@ParamMetaData(type = ObjectType.STRING, description = "{{prefixStartsWith}}") Var prefixStartsWith) {
		return Var.valueOf(textStarts.getObjectAsString().startsWith(prefixStartsWith.getObjectAsString()));
	}

	private static boolean validationReplace(@ParamMetaData(type = ObjectType.STRING, description = "{{textReplace}}") Var textReplace, @ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceRegex}}") Var regex, @ParamMetaData(type = ObjectType.STRING, description = "{{textReplaceReplacement}}") Var replacement) {
		if (textReplace == Var.VAR_NULL || regex == Var.VAR_NULL || replacement == Var.VAR_NULL)
			return true;
		return textReplace.isEmptyOrNull() || regex.isEmptyOrNull();
	}


}
