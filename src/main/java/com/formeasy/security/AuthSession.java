package com.formeasy.security;

import com.formeasy.domain.Usuario;

// Essa classe é responsável por gerenciar a sessão de autenticação do usuário em uma aplicação.
public class AuthSession {
	
    private static String token; // Armazena o token JWT (ou similar) gerado após a autenticação do usuário
    private static String userLogin; // Armazena o login (e-mail) do usuário autenticado.
    private static String userPassword; // Armazena a senha do usuário (não é uma prática recomendada por questão de segurança).
    private static Usuario usuario; // Armazena um objeto Usuario, que pode conter mais detalhes sobre o usuário autenticado.

    // Define o token de autenticação.
    public static void setToken(String jwtToken) {
        token = jwtToken;
    }
    
    // Retorna o token armazenado
    public static String getToken() {
        return token;
    }
    
    // Verifica se o usuário está autenticado, verificando se o token não é nulo e não está vazio.
    public static boolean isAuthenticated() {
        return token != null && !token.isEmpty();
    }
    
    // Realiza o logout do usuário, limpando todas as informações da sessão. 
    // Chamado quando o usuário deseja sair da aplicação ou mudar de conta.
    public static void logout() {
        token = null;
        userLogin = null;
        userPassword = null;
        usuario = null;
    }
    
    // Define o login e a senha do usuário de uma só vez.
    public static void setUserCredentials(String login, String password) {
        AuthSession.userLogin = login;
        AuthSession.userPassword = password;
    }
    
    // Retorna o login armazenado
    public static String getUserLogin() {
        return userLogin;
    }
    
    // Define o login da autenticação
    public static void setUserLogin(String login) {
    	AuthSession.userLogin = login;
    }
 
    // Retorna a senha armazenada
    public static String getUserPassword() {
        return userPassword;
    }
    
    // Define a senha da autenticação
    public static void setUserPassword(String password) {
    	AuthSession.userPassword = password;
    }
    
    // Retorna o usuário armazenado
    public static Usuario getUser() {
        return usuario;
    }
    
    // Define o usuário da autenticação
    public static void setUser(Usuario usuario) {
    	AuthSession.usuario = usuario;
    }
    
}
