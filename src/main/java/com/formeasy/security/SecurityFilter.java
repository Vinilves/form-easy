package com.formeasy.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.formeasy.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// OncePerRequestFilter: Classe do Spring Security que garante que o filtro seja executado uma vez por requisição.
@Component
public class SecurityFilter extends OncePerRequestFilter{
	
	@Autowired
	TokenService tokenService;
	
	@Autowired
	UsuarioRepository usuarioRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
		//Chama o método recoverToken para extrair o token JWT do cabeçalho Authorization da requisição.
		var token = this.recoverToken(request);
		
		// Se o token existir, ele é validado pelo TokenService, que retorna o login do usuário.
		if(token != null) {
		
			// O login é usado para buscar os detalhes do usuário (UserDetails) no banco de dados através do UsuarioRepository.
			var login = tokenService.validateToken(token);
			UserDetails usuario = usuarioRepository.findByLogin(login);
		
			// Cria um objeto UsernamePasswordAuthenticationToken com os detalhes do usuário e suas autoridades (roles).
			var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
		
			// Define o objeto de autenticação no SecurityContextHolder, que é usado pelo Spring Security para gerenciar a autenticação do usuário durante a requisição.
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		// Chama filterChain.doFilter(request, response) para continuar o processamento da requisição pelos próximos filtros.
		filterChain.doFilter(request, response);
	}
	
	// Extrai o token JWT do cabeçalho Authorization da requisição.
	private String recoverToken(HttpServletRequest request) {
		var authHeader = request.getHeader("Authorization");
		
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}
		
		return authHeader.replace("Bearer ", "");
	}
	
}


