package es.jmmanzano.enviaNotificaciones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.jmmanzano.enviaNotificaciones.clases.EmailData;
@Component
public class PdfReader {
	@Autowired
    private EmailService emailService;
	
	private String from = "correo@gmail.com";
	private String pdfRoute = "/home/usuario/pdf_generated/";
	private String pdfName;
	private String csvName;
	private int numPag;
	private ArrayList<EmailData> listaEmailData = new ArrayList<EmailData>();
	private ArrayList<Mail> listaEmails = new ArrayList<Mail>();
	private HashMap<Integer, String> listaDocumentos = new HashMap<>();
	private String cuerpoMensaje = "";
	private void cargaCsv() {
        String line = "";
        String cvsSplitBy = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(csvName))) {
            while ((line = br.readLine()) != null) {
                String[] emailData = line.split(cvsSplitBy);
                if(emailData[2].contains("@")) {
                	listaEmailData.add(new EmailData(emailData[0], emailData[1], emailData[2]));
                }
            }
            cargaFichero();

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	/**
	 * Lee un fichero pdf y busca una cadena PNNN (N = numero) 
	 */
	private void cargaFichero() {
		File file = new File(pdfName);
		try {
			PDDocument pdfDoc = PDDocument.load(file);
			int numPages = pdfDoc.getNumberOfPages();
			Pattern p = Pattern.compile("\\b[P]([0-9]{3})");
			//Pattern p = Pattern.compile("\\n(\\d{3})\\n");
 			for (int i = 0; i < numPages; i+=numPag) {
				
				PDDocument pagina = new PDDocument();
				for(int j = 0; j < numPag; j++) {
					pagina.addPage(pdfDoc.getPage(i+j));
				}
	            PDFTextStripper tStripper = new PDFTextStripper();
	            String pdfFileInText = tStripper.getText(pagina);
	            Matcher m = p.matcher(pdfFileInText);
	            if(m.find()) {
	            	String ruta = pdfRoute+"recibo_"+m.group(1)+".pdf";
	            	listaDocumentos.put(Integer.parseInt(m.group(1)), ruta);
	            	pagina.save(ruta);
	            }
	            pagina.close();
			}
 			for (Iterator<EmailData> iterator = listaEmailData.iterator(); iterator.hasNext();) {
				EmailData emailData = (EmailData) iterator.next();
				String ruta = listaDocumentos.get(Integer.parseInt(emailData.getNumero()));
				String subject = "Recibo "+emailData.getNumero();
				if(null != ruta) {
					try {
						this.listaEmails.add(this.enviaEmail(ruta, emailData.getEmail(), subject ));
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			   
				
			}

		} catch (InvalidPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Mail enviaEmail(String attachment, String to, String subject) throws MessagingException {
		Mail mail = new Mail();
		String texto = this.cuerpoMensaje;
		mail.setFrom(from);
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setContent(texto);
		mail.setAttachment(attachment);
		return mail;
	}
	
	public void setPdf(String pathPdf, String completePdfFileName, String completeCsvFileName, String completeHtmlRoute, Integer paso) {
		this.pdfName = completePdfFileName;
		this.csvName = completeCsvFileName;
		this.numPag = paso.intValue();
                this.readCuerpoMensaje(completeHtmlRoute);
		this.cargaCsv();
		
	}
	public void enviar() {
		this.listaEmails.forEach(x -> {
			try {
				emailService.sendAttachmentMessage(x, x.getAttachment());
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		});
	}
	
	public ArrayList<EmailData> getListaMail(){
		return this.listaEmailData;
	}
	
	public void clearFiles() {
		this.listaDocumentos.forEach((k, v) -> {
			File fichero = new File(v);
			fichero.delete();
		});
	}
	private void readCuerpoMensaje(String fileRoute) {
		StringBuilder contentBuilder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(fileRoute));
		    String str;
		    while ((str = in.readLine()) != null) {
		        contentBuilder.append(str);
		        contentBuilder.append("\n");
		    }
		    in.close();
		} catch (IOException e) {
		}
		this.cuerpoMensaje = contentBuilder.toString();
		System.out.println(this.cuerpoMensaje);
	}

}
