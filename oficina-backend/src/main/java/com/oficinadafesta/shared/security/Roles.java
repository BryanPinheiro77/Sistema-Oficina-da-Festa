package com.oficinadafesta.shared.security;

public final class Roles {
    private Roles() {}

    //SETORES
    public static final String SOMENTE_ADMIN = "hasRole('ADMIN')";

    public static final String CAIXA_OU_ADMIN = "hasAnyRole('CAIXA','ADMIN')";

    public static final String TODOS_SETORES =
            "hasAnyRole('ADMIN','CAIXA','CAFE','CONFEITARIA','PRODUCAO_DOCINHOS','PRODUCAO_SALGADOS','FRITURA','SOBREMESAS','COMUNICACAO')";

    public static final String SETORES_PRODUCAO =
            "hasAnyRole('ADMIN','CAFE','CONFEITARIA','PRODUCAO_DOCINHOS','PRODUCAO_SALGADOS','FRITURA','SOBREMESAS')";

    public static final String CAFE_CAIXA_ADMIN =
            "hasAnyRole('CAFE','CAIXA','ADMIN')";

    public static final String EXPEDICAO =
            "hasAnyRole('ADMIN','CAIXA','RETIRADA')";

    //CLIENTES

    public static final String CLIENTE_CRIAR =
            "hasAnyRole('ADMIN','CAIXA','COMUNICACAO')";

    public static final String CLIENTE_LER =
            "hasAnyRole('ADMIN','CAIXA','CAFE','COMUNICACAO','RETIRADA')";

    // COMANDAS

    public static final String COMANDA_LER =
            "hasAnyRole('ADMIN','CAIXA','CAFE','RETIRADA','COMUNICACAO')";

    public static final String COMANDA_PAGAR_FECHAR =
            "hasAnyRole('ADMIN','CAIXA')";
}