package com.radiocom.auth.domain.model;

/**
 * Perfis de acesso do sistema.
 *
 * ADMIN    — acesso total: gerencia usuários e todos os módulos
 * TECNICO  — acesso operacional: estoque, orçamentos, ordens de serviço
 * AUXILIAR — acesso limitado: consultas e criação de orçamento, sem aprovação
 */
public enum RoleUsuario {
    ADMIN,
    TECNICO,
    AUXILIAR
}
