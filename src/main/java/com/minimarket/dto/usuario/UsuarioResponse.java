package com.minimarket.dto.usuario;

import com.minimarket.entity.Usuario;

import java.util.Set;
import java.util.stream.Collectors;

public class UsuarioResponse {

    private Long id;
    private String username;
    private Set<String> roles;

    public UsuarioResponse(Long id, String username, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public static UsuarioResponse fromEntity(Usuario usuario) {
        Set<String> roles = usuario.getRoles().stream()
                .map(rol -> rol.getNombre())
                .collect(Collectors.toSet());
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), roles);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
