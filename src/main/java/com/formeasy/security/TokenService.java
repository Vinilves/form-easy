package com.formeasy.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.formeasy.domain.Usuario;

@Service
public class TokenService {
	
	//O valor injetado está no applicationproperties
	@Value("${api.security.token.secret}")	
	private String secret;
	
	// Método para gerar o token
	public String generateToken(Usuario usuario) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			String token = JWT.create()
					.withIssuer("auth-api") // Define o emissor do token
					.withSubject(usuario.getLogin()) // Define o assunto do token, que é o login do usuário.
                    .withClaim("id", usuario.getId()) // Adiciona o ID do usuário como uma claim personalizada.
                    .withClaim("role", usuario.getRole().toString()) // Adiciona o papel (role) do usuário.
					.withExpiresAt(generateExpirationDate()) // Define a data de expiração do token usando o método generateExpirationDate.
					.sign(algorithm);
			
			return token;
		}catch(JWTCreationException exception) {
			throw new RuntimeException("Erro ao gerar o token", exception);
		}
	}
	
	// Valida um token JWT e retorna o assunto (subject) do token, que é o login do usuário.
	public String validateToken(String token) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.require(algorithm)
					.withIssuer("auth-api")
					.build()
					.verify(token)
					.getSubject();
		}catch(JWTVerificationException exception) {
			return "";
		}
	}
	
	// Gera a data de expiração do token.
	// Adiciona 2 horas à data e hora atuais.
	// Converte para Instant usando o fuso horário UTC-3 (Brasília).
	private Instant generateExpirationDate() {
		return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
	}

}

