package com.formeasy.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	
	// Um filtro personalizado para validação de tokens JWT que é injetado nesta classe
	// para ser adicionado à cadeia de filtros do Spring Security.
	@Autowired
	SecurityFilter securityFilter;
	
	//Metódo "sessionManagement"
	//Configura a aplicação para ser stateless, ou seja, não mantém estado de sessão no servidor. 
	//Isso é comum em APIs RESTful que usam tokens JWT.
	
	//Método "authorizeHttpRequests" 
	//Define as regras de autorização
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
				.csrf(csrf -> csrf.disable()) // Desabilita CSRF
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sessão stateless
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/auth/login").permitAll() // Permite acesso público ao endpoint de login
						.requestMatchers(HttpMethod.POST, "/auth/registro").permitAll() // Permite acesso público ao endpoint de registro
						.anyRequest().authenticated() // Exige autenticação para qualquer outra requisição
				)
				// Adiciona o filtro personalizado antes do filtro de autenticação padrão
				.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}
	
	//AuthenticationManager é um Bean responsável por gerenciar a autenticação na aplicação. 
	//Ele é obtido a partir da configuração de autenticação do Spring Security (AuthenticationConfiguration).
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	//BCryptPasswordEncode define um codificador de senhas usando o algoritmo BCrypt, 
	//que é seguro e amplamente utilizado para armazenar senhas de forma hasheada.
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(); 
	}

}
