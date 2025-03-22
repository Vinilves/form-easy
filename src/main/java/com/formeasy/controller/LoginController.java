package com.formeasy.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.formeasy.domain.AuthenticationDTO;
import com.formeasy.domain.LoginResponseDTO;
import com.formeasy.security.AuthSession;
import com.formeasy.security.TokenService;

import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.sheets.v4.SheetsScopes;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.rgielen.fxweaver.core.FxmlView;

@Component
@FxmlView("LoginView.fxml")
public class LoginController{
	RedirectController redirect = new RedirectController();

    @Autowired
    private TokenService tokenService;
	
	@FXML
	private Button btnLogin;
	 
    @FXML
    private Button btnLoginGoogle; 
    
    @FXML
    private TextField txtLogin;
    
    @FXML
    private PasswordField txtPassword;
    
    @FXML
    public void initialize() {
      
    }

    
    @FXML
    void onClickLogin(ActionEvent event) throws IOException {
    	
    	// O path funcionou somente com "WelcomeView.fxml"
    	// Este método ficará assim apenas para fins de teste.
    	
    	String login = txtLogin.getText().trim();
    	String password = txtPassword.getText().trim();
    	
    	 if (login.isEmpty() || password.isEmpty()) {
             showNotification("Erro", "Por favor, preencha todos os campos.", false);
             return;
         }
    	 
    	// Dados para autenticação
         AuthenticationDTO authDTO = new AuthenticationDTO(login, password);
    	 
    	 try {
    		// Faz a requisição HTTP para o AuthController
             ResponseEntity<LoginResponseDTO> response = fazerRequisicaoLogin(authDTO);

             if (response.getStatusCode() == HttpStatus.OK) {
                 // Login bem-sucedido
                 String token = response.getBody().token();
                 
                 // Inicia a sessão, buscando as credenciais que o usuário já registrou (entrada rápida)
                 AuthSession.setToken(token);
                 AuthSession.setUserLogin(txtLogin.getText());
                 AuthSession.setUserPassword(txtPassword.getText());                 

                 // Redireciona para a próxima tela
                 redirect.loadNewStage("Menu", "WelcomeView.fxml");
                 redirect.closeCurrentStage(btnLogin);
             } else {
                 // Exibe mensagem de erro
                 showNotification("Erro", "Login ou senha incorretos.", false);
             }
         } catch (Exception e) {
             showNotification("Erro", "Ocorreu um erro ao tentar fazer login.", false);
             e.printStackTrace();
         }
    	 
    }   

    @FXML
    void onClickLoginGoogle(ActionEvent event) throws IOException, URISyntaxException{
    	System.out.println("Botão clicado!");    	
    	openSuperimposedLoginView("http://localhost:8080/attributesuser"); 	
    }
    
    private void openSuperimposedLoginView(String url) {
    	final String CLIENT_ID = "714573222235-bavel8mv55lm80o9e18gbdo1rms32kfk.apps.googleusercontent.com";
    	final String REDIRECT_URI = "http://localhost:8888/Callback";
    	final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE, 
    			PeopleServiceScopes.USERINFO_EMAIL, PeopleServiceScopes.USERINFO_PROFILE);
    	
    	WebView view = new WebView();
    	WebEngine engine = view.getEngine();
    	BorderPane root = new BorderPane(view);
    	TextField redirecionamentoUrl = new TextField(url);
    	
    	redirecionamentoUrl.setOnAction(event -> engine.load(redirecionamentoUrl.getText()));    	
    	engine.locationProperty().addListener((observable, oldValue, newValue) ->
    			redirecionamentoUrl.setText(newValue));
    	
    	Stage stage = new Stage();
    	Image icon = new Image(getClass().getResourceAsStream("/images/logo-quadrada2.png"));
    	stage.setTitle("Fazer login com o Google");
    	stage.getIcons().add(icon);
    	stage.setScene(new Scene(root, 1000, 700));
    	stage.resizableProperty().setValue(Boolean.FALSE);
    	stage.show();
    	
    	String urlAuthentication = "https://accounts.google.com/o/oauth2/auth?access_type=offline&"
                + "client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code&scope=" + SCOPES.get(0) + "%20" + SCOPES.get(1) + "%20" 
                + SCOPES.get(2) + "%20" + SCOPES.get(3);
    	
    	engine.load(url);
    	engine.load(urlAuthentication);
    	
    	
    	engine.locationProperty().addListener((observable, oldValue, newValue) -> {
    		if(newValue.contains("code=")) {
    			String authCode = newValue.split("code=")[1];
    			
    			if(authCode.contains("&")) {
    				authCode = authCode.split("&")[0];
    			}
    			
        		System.out.println("Código de autorização: " + authCode);        		
        		stage.close();
        		
        		try {        	
        			//Faz a requisição HTTP para o AuthController capturando o código de autorização (authCode) retornado pelo Google.
                    ResponseEntity<LoginResponseDTO> response = fazerRequisicaoRegistroGoogle(authCode);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        // Registro e autenticação bem-sucedidos
                        String token = response.getBody().token();
                        String userLogin = response.getBody().login();
                        
                        // Inicia a sessão buscando as credenciais no AuthSession
                        AuthSession.setToken(token);
                        AuthSession.setUserLogin(userLogin);                                       

                        // Redireciona para a próxima tela
                        redirect.loadNewStage("Menu", "WelcomeView.fxml");
                        redirect.closeCurrentStage(btnLogin);
                    } else {
                        showNotification("Erro", "Falha na autenticação com o Google.", false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showNotification("Erro", "Falha na autenticação com o Google.", false);
                }
            } else {
                System.out.println("Erro, Nenhum código encontrado ainda...");
            }
        });
    }
    
    //Envia requisições HTTP para o backend para autenticação
    private ResponseEntity<LoginResponseDTO> fazerRequisicaoLogin(AuthenticationDTO authDTO) {
        // Configuração do cliente HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Corpo da requisição
        HttpEntity<AuthenticationDTO> request = new HttpEntity<>(authDTO, headers);
        
        ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity("http://localhost:8080/auth/login", request, LoginResponseDTO.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            LoginResponseDTO loginData = response.getBody();
            System.out.println("Token: " + loginData.token());
            System.out.println("Login: " + loginData.login());
            System.out.println("Senha: " +loginData.password());
            
        }

        return response;
    }
    
    //Envia requisições HTTP para o backend para registro.
    private ResponseEntity<LoginResponseDTO> fazerRequisicaoRegistroGoogle(String authCode) {
        // Configuração do cliente HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Corpo da requisição (envia o authCode como uma string)
        HttpEntity<String> request = new HttpEntity<>(authCode, headers);

        ResponseEntity<LoginResponseDTO> response = restTemplate.postForEntity("http://localhost:8080/auth/registro", request, LoginResponseDTO.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            LoginResponseDTO loginData = response.getBody();
            System.out.println("Token gerado: " + loginData.token());
            System.out.println("Login recebido: " + loginData.login());
            System.out.println("Senha recebida: " + loginData.password());
        }

        return response;
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