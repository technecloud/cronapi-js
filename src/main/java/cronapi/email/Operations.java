package cronapi.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

	@CronapiMetaData(type = "function", name = "{{sendEmailSmtp}}", nameTags = {
			"sendEmailSmtp" }, description = "{{functionToSendEmailSmtp}}", params = { "{{hostAddress}}",
					"{{hostPort}}", "{{protocolToSendEmail}}", "{{login}}", "{{password}}", "{{senderMail}}",
					"{{toRecipientMail}}", "{{subject}}", "{{content}}", "{{isHtml}}", "{{attachments}}" }, paramsType = {
							ObjectType.STRING, ObjectType.STRING, ObjectType.STRING, ObjectType.STRING,
							ObjectType.STRING, ObjectType.STRING, ObjectType.OBJECT, ObjectType.STRING,
							ObjectType.STRING, ObjectType.BOOLEAN, ObjectType.OBJECT }, returnType = ObjectType.BOOLEAN)
	public static final Var sendEmailSmtp(Var hostAddress, Var hostPort, Var protocolType, Var login, Var password,
			Var from, Var to, Var subject, Var content, Var isHtml, Var attachments) throws Exception {

		String host = hostAddress.getObjectAsString().trim();
		final String user = login.getObjectAsString().trim();
		final String pass = password.getObjectAsString().trim();
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pass);
			}
		};

		String port = hostPort.getObjectAsString().trim();
		String protocol = protocolType.getObjectAsString().trim().toUpperCase();
		if (port.isEmpty()) {
			if ("TLS".equals(protocol))
				port = "587";
			else if ("SSL".equals(protocol))
				port = "465";
			else
				port = "25";
		}

		System.setProperty("line.separator", "\r\n");
		Properties props = new Properties();
		props.put("mail.debug", "false");
		props.put("mail.smtp.host", host);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", port);
		if ("TLS".equals(protocol))
			props.put("mail.smtp.starttls.enable", "true");
		else if ("SSL".equals(protocol))
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.user", user);
		props.put("mail.smtp.password", pass);

		String fromMail = from.getObjectAsString().trim();
		Pattern pattern = Pattern.compile("(.+)?<(.+?)>");
		Matcher matcher = pattern.matcher(fromMail);
		InternetAddress fromAddress;
		if (matcher.find())
			fromAddress = new InternetAddress(matcher.group(2), matcher.group(1));
		else
			fromAddress = new InternetAddress(fromMail);
		Session session = Session.getInstance(props, auth);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(fromAddress);
		if (to.getObject() instanceof List) {
			@SuppressWarnings("unchecked")
			List<Var> recipients = (List<Var>) to.getObject();
			for (Var rec : recipients) {
				Recipient recipient = (Recipient) rec.getObject();
				if (recipient.type == Recipient.Type.TO)
					message.addRecipient(RecipientType.TO, new InternetAddress(recipient.to));
				else if (recipient.type == Recipient.Type.CC)
					message.addRecipient(RecipientType.CC, new InternetAddress(recipient.to));
				else
					message.addRecipient(RecipientType.BCC, new InternetAddress(recipient.to));
			}
		} else if (to.getObject() instanceof String) {
			String mail = to.getObjectAsString().trim();
			if (!mail.contains(","))
				message.addRecipient(RecipientType.TO, new InternetAddress(mail));
			else {
				String[] splited = mail.split(",");
				for (String recipient : splited)
					message.addRecipient(RecipientType.TO, new InternetAddress(recipient.trim()));
			}
		}
		message.setSubject(subject.getObjectAsString());
		
		javax.mail.Multipart messageBody = new javax.mail.internet.MimeMultipart();
		javax.mail.internet.MimeBodyPart bodyPart = new javax.mail.internet.MimeBodyPart();
		bodyPart.setContent(content.getObjectAsString(), (isHtml.getObjectAsBoolean() ? "text/html" : "text/plain"));
		messageBody.addBodyPart(bodyPart);

		if (attachments.getObject() != null) {
			javax.mail.internet.MimeBodyPart attachBodyPart = null;
			if (attachments.getObject() instanceof List) {
				@SuppressWarnings("unchecked")
				List<Var> attachs = (List<Var>) attachments.getObject();
				for (Var pathAttachment : attachs) {
					if (!pathAttachment.getObjectAsString().isEmpty()) {
						javax.activation.FileDataSource fds = new javax.activation.FileDataSource(pathAttachment.getObjectAsString().trim());
						attachBodyPart = new javax.mail.internet.MimeBodyPart();
						attachBodyPart.setDataHandler(new javax.activation.DataHandler(fds));
						attachBodyPart.setFileName(fds.getName());
						messageBody.addBodyPart(attachBodyPart);
					}
				}
			} else if (attachments.getObject() instanceof String) {
				String pathAttachment = attachments.getObjectAsString().trim();
				if (!pathAttachment.isEmpty()) {
					javax.activation.FileDataSource fds = new javax.activation.FileDataSource(pathAttachment);
					attachBodyPart = new javax.mail.internet.MimeBodyPart();
					attachBodyPart.setDataHandler(new javax.activation.DataHandler(fds));
					attachBodyPart.setFileName(fds.getName());
					messageBody.addBodyPart(attachBodyPart);
				}
			}
		}
		message.setContent(messageBody);
		Transport.send(message);
		return new Var(true);
	}

}
