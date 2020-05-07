package cronapi.regex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cronapi.CronapiMetaData;
import cronapi.ParamMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.i18n.Messages;

/**
 * Classe que representa ...
 *
 * @author Samuel Almeida
 * @version 1.0
 * @since 2020-04-14
 *
 */

@CronapiMetaData(category = CategoryType.REGEX, categoryTags = {  "Expressão Regular", "Regular Expression" })
public class Operations {


    @CronapiMetaData(type = "function", name = "{{extractTextWithRegex}}", nameTags = { "extractTextWithRegex" },
            description = "{{extractTextWithRegexDescription}}", params = { "{{text}}", "{{regex}}", "{{flag}}" },
            paramsType = { ObjectType.STRING, ObjectType.STRING, ObjectType.OBJECT}, returnType = ObjectType.LIST)
    public static final Var extractTextWithRegex(Var text, Var regex, @ParamMetaData(type = ObjectType.OBJECT, description = "{{flag}}", blockType = "util_dropdown",
            keys = {"CASE_INSENSITIVE", "MULTILINE", "DOTALL", "UNICODE_CASE", "CANON_EQ", "UNIX_LINES", "LITERAL", "UNICODE_CHARACTER_CLASS", "COMMENTS"},
            values = {"{{CASE_INSENSITIVE}}", "{{MULTILINE}}", "{{DOTALL}}", "{{UNICODE_CASE}}", "{{CANON_EQ}}", "{{UNIX_LINES}}", "{{LITERAL}}", "{{UNICODE_CHARACTER_CLASS}}", "{{COMMENTS}}"}) Var flags) throws Exception{

        if(Var.valueOf(regex).isEmptyOrNull()) return Var.newList();

        Pattern pattern = getPattern(regex, flags);

        return getMatcherResults(pattern, text);

    }

    @CronapiMetaData(type = "function", name = "{{validateTextWithRegex}}", nameTags = { "validateTextWithRegex"},
            description = "{{validateTextWithRegexDescription}}", params = { "{{text}}", "{{regex}}", "{{flag}}" },
            paramsType = { ObjectType.STRING, ObjectType.STRING, ObjectType.OBJECT}, returnType = ObjectType.BOOLEAN)
    public static final Var validateTextWithRegex(Var text, Var regex, @ParamMetaData(type = ObjectType.OBJECT, description = "{{flag}}", blockType = "util_dropdown",
            keys = {"CASE_INSENSITIVE", "MULTILINE", "DOTALL", "UNICODE_CASE", "CANON_EQ", "UNIX_LINES", "LITERAL", "UNICODE_CHARACTER_CLASS", "COMMENTS"},
            values = {"{{CASE_INSENSITIVE}}", "{{MULTILINE}}", "{{DOTALL}}", "{{UNICODE_CASE}}", "{{CANON_EQ}}", "{{UNIX_LINES}}", "{{LITERAL}}", "{{UNICODE_CHARACTER_CLASS}}", "{{COMMENTS}}"}) Var flags) throws Exception{


        if(Var.valueOf(regex).isEmptyOrNull()) return Var.VAR_FALSE;

        Pattern pattern = getPattern(regex, flags);

        return existsMatches(pattern, text);


    }

    private static Pattern getPattern(Var regex, Var flags) throws Exception {

        if(Var.valueOf(flags).isNull()){

            return Pattern.compile(org.apache.commons.lang3.StringEscapeUtils.unescapeJava( regex.getObjectAsString()));

        }else{

            Integer patternFlag = getFlags(flags);

            if(Var.valueOf(patternFlag).isNull())
                throw new Exception(Messages.getString("flagRegexError"));


            return Pattern.compile(org.apache.commons.lang3.StringEscapeUtils.unescapeJava(regex.getObjectAsString()), patternFlag);
        }
    }

    private static Integer getFlags (Var flags) throws Exception{


        try {

            if (flags.getObject() instanceof String) {

                return PatternFlags.valueOf(Var.valueOf(flags).toString()).getValue();

            } else if (flags.getObject() instanceof List) {

                Integer flagValue = null;

                for (Object flag : flags.getObjectAsList()) {

                    if(!(Var.valueOf(flag).getObject() instanceof String))
                        throw new Exception(Messages.getString("flagRegexError"));

                    if(Var.valueOf(flagValue).isNull())
                        flagValue = PatternFlags.valueOf(Var.valueOf(flag).toString()).getValue();
                    else
                        flagValue = flagValue | PatternFlags.valueOf(Var.valueOf(flag).toString()).getValue();

                }

                return flagValue;

            }

        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(Messages.getString("flagRegexError"));
        }

        return null;

    }

    private static Var getMatcherResults(Pattern pattern, Var text) {

        if (pattern == null)
            return Var.newList();

        Matcher matcher = pattern.matcher(text.getObjectAsString());
        List<Var> list = null;
        List<List<Var>> list2 = new LinkedList<List<Var>>();

        while (matcher.find()) {

            list = new LinkedList<Var>();

            for (int i = 1; i <= matcher.groupCount() ; i++) {
                list.add(new Var(matcher.group(i)));
            }

            list2.add(list);
        }


        return Var.valueOf(list2);
    }


    private static Var existsMatches(Pattern pattern, Var text) {

        if (pattern == null)
            return Var.VAR_FALSE;

        Matcher matcher = pattern.matcher(text.getObjectAsString());

        return Var.valueOf(matcher.matches());
    }

}
