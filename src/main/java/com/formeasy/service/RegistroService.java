package com.formeasy.service;

import com.formeasy.domain.Usuario;
import com.formeasy.domain.UsuarioRole;
import com.formeasy.repository.UsuarioRepository;
import com.formeasy.util.GoogleOAuthHelper;
import com.formeasy.util.GooglePeopleHelper;
import com.google.api.services.people.v1.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
public class RegistroService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
	public BCryptPasswordEncoder passwordEncoder;
    
    // Gerar um senha de app que vai ser criptografada e armazenada no banco, logo após o registro de um novo usuário
    public String gerarSenhaDeAplicativo() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * @param login O login (e-mail) do usuário.
     * @param senha A senha fornecida pelo usuário.
     * @throws IllegalArgumentException Se o usuário já existir.
     */
    public Usuario registrarUsuarioGoogle(@RequestBody String authCode) throws Exception {
        // Trocar o código de autorização por um token de acesso
        String accessToken = GoogleOAuthHelper.getAccessToken(authCode);

        // Obter as informações do usuário usando o token de acesso
        Person userInfo = GooglePeopleHelper.getUserInfo(accessToken);

        if (userInfo == null || userInfo.getEmailAddresses() == null || userInfo.getEmailAddresses().isEmpty()) {
            throw new IllegalArgumentException("Não foi possível obter o e-mail do usuário.");
        }

        String login = userInfo.getEmailAddresses().get(0).getValue();

        // Verifica se o usuário já está registrado
        Usuario usuario = (Usuario) usuarioRepository.findByLogin(login);

        if (usuario != null) {
            return usuario; // Usuário já registrado
        } else {
        	
        	String appPassword = gerarSenhaDeAplicativo();
        	
            // Criar novo usuário
            Usuario newUsuario = new Usuario();
            newUsuario.setLogin(login);
            newUsuario.setPassword(passwordEncoder.encode(appPassword)); // Senha do app criptografada
            newUsuario.setRole(UsuarioRole.USER); // Papel padrão

            return usuarioRepository.save(newUsuario);
        }
    }
    
}

