package cronapi.validation;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.Var;

/**
 * Classe que representa ...
 *
 * @author Samuel Almeida
 * @version 1.0
 * @since 2020-04-14
 *
 */

@CronapiMetaData(category = CategoryType.VALIDATION, categoryTags = {  "Validation", "Validação" })
public class Operations {


    @CronapiMetaData(type = "function", name = "{{validateCPF}}", nameTags = {"validateCPF"},
            description = "{{validateCPFDescription}}", params = {"{{cpf}}"},
            paramsType = {ObjectType.STRING}, returnType = ObjectType.BOOLEAN)
    public static final Var validateCPF(Var cpf) {

        if (Var.valueOf(cpf).isEmptyOrNull()) return Var.VAR_FALSE;

        return isValidCPF(Var.valueOf(cpf).toString());
    }

    @CronapiMetaData(type = "function", name = "{{validateCNPJ}}", nameTags = {"validateCNPJ"},
            description = "{{validateCNPJDescription}}", params = {"{{cnpj}}"},
            paramsType = {ObjectType.STRING}, returnType = ObjectType.BOOLEAN)
    public static final Var validateCNPJ(Var cnpj) {

        if (Var.valueOf(cnpj).isEmptyOrNull()) return Var.VAR_FALSE;

        return isValidCNPJ(Var.valueOf(cnpj).toString());

    }

    @CronapiMetaData(type = "function", name = "{{validateEmail}}", nameTags = {"validateEmail"},
            description = "{{validateEmailDescription}}", params = {"{{email}}"},
            paramsType = {ObjectType.STRING}, returnType = ObjectType.BOOLEAN)
    public static final Var validateEmail(Var email) throws Exception{
        return cronapi.regex.Operations.validateTextWithRegex(email, new Var("^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"),
                new Var("CASE_INSENSITIVE"));
    }



    private static final Var isValidCPF(String cpf) {

        int[] weightCPF = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

        cpf = cpf.trim().replace(".", "").replace("-", "");
        if (cpf.length() != 11) return Var.VAR_FALSE;

        for (int j = 0; j < 10; j++)
            if (padLeft(Integer.toString(j), Character.forDigit(j, 10)).equals(cpf))
                return Var.VAR_FALSE;

        Integer digit1 = calculateDigit(cpf.substring(0, 9), weightCPF);
        Integer digit2 = calculateDigit(cpf.substring(0, 9) + digit1, weightCPF);
        Boolean result = cpf.equals(cpf.substring(0, 9) + digit1.toString() + digit2.toString());

        return Var.valueOf(result);
    }

    private static final Var isValidCNPJ(String cnpj) {

        int[] weightCNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        cnpj = cnpj.trim().replace(".", "").replace("-", "").replace("/", "");
        if (cnpj.length()!=14) return Var.VAR_FALSE;

        Integer digit1 = calculateDigit(cnpj.substring(0,12), weightCNPJ);
        Integer digit2 = calculateDigit(cnpj.substring(0,12) + digit1, weightCNPJ);
        Boolean result =  cnpj.equals(cnpj.substring(0,12) + digit1.toString() + digit2.toString());

        return Var.valueOf(result);
    }


    private static final String padLeft(String text, char character) {
        return String.format("%11s", text).replace(' ', character);
    }

    private static int calculateDigit(String str, int[] weight) {
        int sum = 0;
        for (int index=str.length()-1, digit; index >= 0; index-- ) {
            digit = Integer.parseInt(str.substring(index,index+1));
            sum += digit * weight[weight.length-str.length()+index];
        }
        sum = 11 - sum % 11;
        return sum > 9 ? 0 : sum;
    }


}
