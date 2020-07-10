package cronapi.email;

import cronapi.Var;
import junit.framework.TestCase;
import org.jvnet.mock_javamail.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class OperationsTest extends TestCase {

    File attachment;

    public void setUp() throws Exception {
        super.setUp();
        Mailbox.clearAll();
        attachment = File.createTempFile("attachment", "file");
    }

    public void testSendEmailMock() throws MessagingException, IOException {
        Var from = Var.valueOf("dev@cronapp.io");
        Var to = Var.valueOf("testeCronapiJava@emailMock.com");
        Var cc = Var.VAR_NULL;
        Var bcc = Var.VAR_NULL;
        Var subject = Var.valueOf("Teste Assunto");
        Var msg = Var.valueOf("Mensagem");
        Var html = Var.VAR_NULL;
        Var attachments = Var.valueOf(attachment.getPath());
        Var smtpHost = Var.valueOf("testmail.com");
        Var smtpPort = Var.VAR_NULL;
        Var login = Var.VAR_NULL;
        Var password = Var.VAR_NULL;
        Var ssl = Var.VAR_NULL;

        Operations.sendEmail(from, to, cc, bcc, subject, msg, html, attachments, smtpHost, smtpPort, login, password, ssl);

        List<Message> inbox = Mailbox.get("testeCronapiJava@emailMock.com");
        assertEquals(1, inbox.size());
        assertEquals(subject.getObjectAsString(), inbox.get(0).getSubject());
        Multipart multiPart = (Multipart) inbox.get(0).getContent();
        for (int i = 0; i < multiPart.getCount(); i++) {
            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                assertTrue(part.getFileName().startsWith("attachment") && part.getFileName().endsWith("file"));
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        attachment.delete();
    }
}