package com.formeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.formeasy.domain.AuthenticationDTO;
import com.formeasy.domain.LoginResponseDTO;
import com.formeasy.domain.Usuario;
import com.formeasy.security.TokenService;
import com.formeasy.service.AutenticacaoService;
import com.formeasy.service.RegistroService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
public class AuthController {
	
	@Autowired
    private AutenticacaoService autenticacaoService;

    @Autowired
    private RegistroService registroService;
	
	@Autowired
	private TokenService tokenService;
	
	
	//Endpoint de Login
	@PostMapping("/login")
	public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
		
		try {
			//Aqui está buscando as informações do usuário(login e senha) para autenticar, a partir da classe "autenticacaoService" 
			//e do método "autenticarUsuario"
            Usuario usuario = autenticacaoService.autenticarUsuario(data.login(), data.password());
            
            //Aqui está recebendo as credenciais do usuário e gerando o token, a partir do "tokenService" e do método "generateToken"
            String token = tokenService.generateToken(usuario);

            return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getLogin(), usuario.getPassword()));
        } catch (AutenticacaoService.UsuarioNaoEncontradoException | AutenticacaoService.SenhaInvalidaException e) {
        	//Caso o usuário não seja autenticado/encontrado, irá imprimir status de não autorizado
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponseDTO(null, null, null));
        }
    }
	
	@PostMapping("/registro")
	public ResponseEntity <LoginResponseDTO> register(@RequestBody @Valid String authCode) {
		
	     try {
	    	 	//Aqui está buscando o código de autorização a partir da classe "registroService" e do método "registrarUsuarioGoogle"
	            Usuario usuario = registroService.registrarUsuarioGoogle(authCode);
	            
	          //Aqui está recebendo as credenciais do usuário e gerando o token, a partir do "tokenService" e do método "generateToken"
	            String token = tokenService.generateToken(usuario);
	            
	            return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getLogin(), usuario.getPassword()));
	        } catch (Exception e) {
	        	//Caso o usuário não seja autenticado/encontrado, irá imprimir status de não autorizado
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDTO(null, null, null));
	        }
	    }
    }



