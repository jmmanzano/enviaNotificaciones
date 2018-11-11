package es.jmmanzano.enviaNotificaciones;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class PdfReader {
	@Autowired
    private EmailService emailService;
	private String from = "from@mail.com";
	private String to = "to@mail.com";
	private String pdfRoute = "/home/josemi/eclipse-workspace/enviaNotificaciones/src/main/resources/";
	private String pdfName = "10_2018.pdf";
	public void cargaFichero() {
		File file = new File(pdfRoute+pdfName);
		try {
			PDDocument pdfDoc = PDDocument.load(file);
			int numPages = pdfDoc.getNumberOfPages();
			int paso = 1;
			System.out.println(numPages);
			Pattern p = Pattern.compile("\\b[P]([0-9]{3})");
			String[] numeros = {"001", "004"};
 			for (int i = 0; i < numPages; i+=2) {
				
				PDDocument pagina = new PDDocument();
				pagina.addPage(pdfDoc.getPage(i));
				pagina.addPage(pdfDoc.getPage(i+1));
				
	            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
	            stripper.setSortByPosition(true);
	
	            PDFTextStripper tStripper = new PDFTextStripper();
	
	            String pdfFileInText = tStripper.getText(pagina);
	            Matcher m = p.matcher(pdfFileInText);
	            if(m.find()) {
	            	boolean contains = Stream.of(numeros).anyMatch(x ->  x.equals(m.group(1)));
	            	if(contains && Integer.parseInt(m.group(1)) == paso) {
	            		String ruta = pdfRoute+"salida_"+(paso)+".pdf";
	            		pagina.save(ruta);
	            		try {
	            			Thread.sleep(1000);
							enviaEmail(ruta);
						} catch (MessagingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	            	}
	            }
	            paso++;
			}

		} catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void enviaEmail() {
		Mail mail = new Mail();
		mail.setFrom(from);
		mail.setTo(to);
		mail.setSubject("Mensaje de ejemplo");
		mail.setContent("contenido de ejemplo");
		emailService.sendSimpleMessage(mail);
	}
	private void enviaEmail(String attachment) throws MessagingException {
		Mail mail = new Mail();
		String texto = "Texto de ejemplo";
		mail.setFrom(from);
		mail.setTo(to);
		mail.setSubject("Mensaje de ejemplo");
		mail.setContent(texto);
		emailService.sendAttachmentMessage(mail, attachment);
	}
	

}
