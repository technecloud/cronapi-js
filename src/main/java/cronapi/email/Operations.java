package cronapi.email;

import java.util.ArrayList;
import java.util.List;

import cronapi.CronapiMetaData;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-10
 *
 */

@CronapiMetaData(category = CategoryType.EMAIL, categoryTags = { "Email" })
public class Operations {

	private static final List<Recipient> transformToRecipient(Var dest, Recipient.Type rec) {
		List<Recipient> recipients = new ArrayList<Recipient>();
		if (!dest.getObjectAsString().isEmpty()) {
			String[] array = dest.getObjectAsString().trim().split(",");
			for (String obj : array)
				recipients.add(new Recipient(obj.trim(), rec));
		}
		return recipients;
	}

	@CronapiMetaData(type = "function", name = "{{createRecipientsAddress}}", nameTags = {
			"createRecipientsAddress" }, description = "{{functionToCreateRecipientsAddress}}", params = {
					"{{forRecipient}}", "{{copyToRecipient}}", "{{hiddenCopyToRecipient}}" }, paramsType = {
							ObjectType.STRING, ObjectType.STRING, ObjectType.STRING }, returnType = ObjectType.OBJECT)
	public static final Var createRecipientsAddress(Var to, Var cc, Var bcc) throws Exception {
		List<Recipient> recipients = new ArrayList<Recipient>();
		List<Recipient> recipientTo = transformToRecipient(to, Recipient.Type.TO);
		List<Recipient> recipientCC = transformToRecipient(cc, Recipient.Type.CC);
		List<Recipient> recipientBCC = transformToRecipient(bcc, Recipient.Type.BCC);
		recipients.addAll(recipientTo);
		recipients.addAll(recipientCC);
		recipients.addAll(recipientBCC);
		return new Var(recipients);
	}

}
