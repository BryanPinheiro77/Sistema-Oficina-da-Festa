package com.oficinadafesta.shared.config;


import com.oficinadafesta.comanda.domain.Comanda;
import com.oficinadafesta.comanda.repository.ComandaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ComandaSeed {

    private final ComandaRepository comandaRepository;

    @PostConstruct
    public void initComandas(){
        if(comandaRepository.count() == 0){
            for (int i = 1; i <= 999; i++){
                String codigo = String.format("%03d", i);
                Comanda comanda = new Comanda();
                comanda.setCodigo(codigo);
                comanda.setAtiva(true);
                comanda.setPaga(true);
                comandaRepository.save(comanda);
            }
            System.out.println("999 Comandas inseridas com sucesso.");
        } else {
            System.out.println("Comandas já existentes. seed ignorada");
        }
    }
}
