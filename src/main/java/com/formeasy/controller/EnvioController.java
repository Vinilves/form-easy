package com.formeasy.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import org.controlsfx.control.Notifications;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.formeasy.service.EmailService;
import com.formeasy.service.ExcelService;

import jakarta.mail.MessagingException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@FxmlView("EmailView.fxml")
public class EnvioController {
	
	RedirectController redirect = new RedirectController();

    @Autowired
    private ExcelService excelService;

    @Autowired
    private EmailService emailService;
    
    @FXML
    private Button btnMenu;
    
    @FXML
    private Button btnAcessResp;
    
    @FXML 
    private Button btnSair;

    @FXML
    private TextField TextAssunto;

    @FXML
    private TextArea TextMensagem;

    @FXML
    private Button btnAdicionarArquivo;

    @FXML
    private Button btnEnviar;

    private File selectedFile;
    
    public EnvioController(){
    	this.excelService = new ExcelService();
    	this.emailService = new EmailService();
    }


    @FXML
    public void initialize() {
    	btnMenu.setOnAction(e-> voltarMenu());
    	btnAcessResp.setOnAction(e-> AcessoRespostas());
    	btnSair.setOnAction(e-> Sair());
        btnAdicionarArquivo.setOnAction(e-> adicionarArquivo());
        btnEnviar.setOnAction(e-> enviarEmails());

    }
        	
    @FXML
    public void voltarMenu() {
        try {
        	redirect.loadNewStage("Menu", "WelcomeView.fxml");
        	redirect.closeCurrentStage(btnMenu);
        } catch (IOException e) {
        	showNotification("Erro", "Erro ao carregar a tela do menu: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    	
    @FXML
    public void AcessoRespostas() {
    	try {
    		redirect.loadNewStage("Analisar Respostas", "ShowAnswersView.fxml");
    		redirect.closeCurrentStage(btnAcessResp);
    	}catch(IOException e) {
    		showNotification("Erro", "Erro ao carregar a tela de análise das respostas" + e.getMessage(), false);
    		e.printStackTrace();
    	}
   
    }
    	
    @FXML
    public void Sair() {
    	 Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    	 alert.setTitle("Confirmação de Saída");
    	 alert.setHeaderText(null);
    	 alert.setContentText("Tem certeza que deseja sair?");
    	 
    	 Image logo = new Image(getClass().getResourceAsStream("/images/logo-quadrada2.png"));
    	 ImageView logoView = new ImageView(logo);
    	 logoView.setFitWidth(20);
    	 logoView.setFitHeight(20);

    	 alert.setGraphic(logoView);

    	 Optional<ButtonType> result = alert.showAndWait();

    	 if (result.isPresent() && result.get() == ButtonType.OK) {
    		 Platform.exit();
    	 }else {
    		 System.out.println("Saída cancelada");
    	    }
    }
    	
    
    @FXML
    public void adicionarArquivo() {
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) btnAdicionarArquivo.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
        	showNotification("Sucesso", "Arquivo selecionado: " + selectedFile.getAbsolutePath(), true);
        } else {
        	showNotification("Erro", "Nenhum arquivo selecionado.", false);
        }
        
        btnAdicionarArquivo.setOnMousePressed(event -> {
            btnAdicionarArquivo.setStyle("-fx-background-color: #bbbbbb; -fx-translate-y: 2px;");
        });

        btnAdicionarArquivo.setOnMouseReleased(event -> {
        	btnAdicionarArquivo.setStyle("-fx-background-color: #dddddd; -fx-translate-y: 0;");
        });
    }

    @FXML
    public void enviarEmails() {
    	    String assunto = TextAssunto.getText().trim();
    	    String descricao = TextMensagem.getText().trim();

    	    if (assunto.isEmpty()) {
    	    	showNotification("Erro", "O assunto não pode estar vazio.", false);
    	        return;
    	    }

    	    if (descricao.isEmpty()) {
    	    	showNotification("Erro", "A mensagem não pode estar vazia.", false);
    	        return;
    	    }

    	    if (selectedFile != null) {
    	        try {
    	            List<String> emails = excelService.getEmailsFromExcel(selectedFile);
    	            emails.removeIf(email -> email == null || email.trim().isEmpty() || !emailService.isValidEmail(email));

    	            if (!emails.isEmpty()) {
    	                emailService.sendEmails(emails, assunto, descricao);
    	                showNotification("Sucesso", emails.size() + " e-mails enviados com sucesso!", true);
    	            } else {
    	            	showNotification("Aviso", "Nenhum e-mail válido encontrado no arquivo.", false);
    	            }
    	        } catch (IOException e) {
    	        	showNotification("Erro", "Erro ao ler o arquivo: " + e.getMessage(), false);
    	            e.printStackTrace();
    	        } catch (MessagingException e) {
    	        	showNotification("Erro", "Erro ao enviar e-mails: " + e.getMessage(), false);
    	            e.printStackTrace();
    	        }
    	    } else {
    	    	showNotification("Aviso", "Nenhum arquivo selecionado.", false);
    	    }
}

    public void showNotification(String titulo, String mensagem, boolean sucesso) {
        
        String imagePath = sucesso ? "/images/sucess.png" : "/images/error.png";

        // Carregar imagens
        Image image = new Image(getClass().getResource(imagePath).toExternalForm());
        
        ImageView imageViewStatus = new ImageView(image);
        if (sucesso) {
            imageViewStatus.setFitWidth(50);  // Tamanho para imagem de sucesso
            imageViewStatus.setFitHeight(50);
        } else {
            imageViewStatus.setFitWidth(80);  // Tamanho para imagem de erro
            imageViewStatus.setFitHeight(80);
        }
        imageViewStatus.setPreserveRatio(true); 

        // Criar e exibir a notificação
        Notifications.create()
              .title(titulo)
              .text(mensagem)
              .graphic(imageViewStatus) 
              .position(Pos.BASELINE_RIGHT)  // Posição no canto inferior direito da tela
              .hideAfter(Duration.seconds(5))  // Duração da notificação
              .show();
         }
     
}