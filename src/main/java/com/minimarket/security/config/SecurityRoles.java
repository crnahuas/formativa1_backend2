package com.minimarket.security.config;

public final class SecurityRoles {

    public static final String CLIENTE = "CLIENTE";
    public static final String EMPLEADO = "EMPLEADO";
    public static final String GERENTE = "GERENTE";

    public static final String ROLE_CLIENTE = "ROLE_" + CLIENTE;
    public static final String ROLE_EMPLEADO = "ROLE_" + EMPLEADO;
    public static final String ROLE_GERENTE = "ROLE_" + GERENTE;

    public static final String HAS_ROLE_GERENTE = "hasRole('GERENTE')";
    public static final String HAS_ROLE_EMPLEADO_OR_GERENTE = "hasAnyRole('EMPLEADO','GERENTE')";
    public static final String HAS_ROLE_CLIENTE_EMPLEADO_OR_GERENTE = "hasAnyRole('CLIENTE','EMPLEADO','GERENTE')";

    private SecurityRoles() {
    }
}
