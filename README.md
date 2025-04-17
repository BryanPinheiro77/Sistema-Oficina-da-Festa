# Oficina da Festa 🎂✨

[![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=java&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](#)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](#)
[![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue?style=for-the-badge&logo=java)](#)
[![Status do Projeto](https://img.shields.io/badge/STATUS-EM%20DESENVOLVIMENTO-yellow?style=for-the-badge)](#)

**Oficina da Festa** é um sistema de gestão completo desenvolvido para uma confeitaria local.  
Ele organiza pedidos, produção por setor, comandas físicas, pagamentos e comunicação com os clientes.  
Criado para otimizar o fluxo entre os setores da confeitaria, melhorar o atendimento e facilitar o gerenciamento interno.

---

## 🌟 Funcionalidades

- ✅ Cadastro de clientes com endereço completo e telefone  
- ✅ Criação de pedidos com cálculo automático de total, taxa de entrega e itens  
- ✅ Acompanhamento de status por **item** (pendente, preparando, pronto)  
- ✅ Direcionamento de itens para setores específicos (doces, fritura, café etc.)  
- ✅ Controle de **comandas físicas** (entrada e saída com liberação via pagamento)  
- ✅ Gestão de pagamentos (Pix, cartão, parcelamento com taxas)  
- ✅ Filtros por **status** e **setor**  
- 🔜 Integração com **WhatsApp** para atendimento  
- 🔜 Painel em tempo real para produção/setores  

---

## 🛠 Tecnologias Utilizadas

- **Java 17**  
- **Spring Boot 3.x**  
- **MySQL 8.0**  
- **JavaFX + Scene Builder**  
- **Maven**

---

## 🗂 Estrutura do Projeto

- `Cliente`: informações do consumidor (nome, telefone, endereço)
- `Produto`: nome, preço, categoria e setor responsável
- `Pedido`: vinculado ao cliente, com forma de pagamento, status e itens
- `ItemPedido`: representa os produtos individuais de cada pedido
- `CategoriaProduto`: agrupa produtos e define a área/setor de produção
- `Comanda`: número físico usado para controlar entrada e saída

---

## 🚀 Como Rodar o Projeto

### ✅ Requisitos

- Java 17 instalado  
- MySQL Server 8.0 rodando  
- IDE com suporte a Maven (IntelliJ, Eclipse, VS Code etc.)  
- Scene Builder (opcional, para editar interfaces FXML)

### 🔧 Configuração

1. Clone o repositório:

```bash
git clone https://github.com/BryanPinheiro77/oficina-da-festa.git
cd oficina-da-festa
Crie o banco de dados MySQL:

CREATE DATABASE oficina_da_festa;
Atualize o arquivo application.properties com suas credenciais:

spring.datasource.url=jdbc:mysql://localhost:3306/oficina_da_festa
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

Rode a aplicação:
./mvnw spring-boot:run
```

📅 Próximas Etapas

🔗 Integração com WhatsApp

📊 Dashboard em tempo real por setor

💳 Tela do caixa com filtros e agendamento

📈 Módulo de relatórios e estatísticas

🎨 Otimização visual da interface JavaFX

🤝 Contribuição
Este projeto é de uso interno, mas sugestões são bem-vindas!
Se quiser contribuir, fique à vontade para abrir uma issue ou enviar um pull request.

📝 Licença
📌 Projeto de uso exclusivo da Oficina da Festa – todos os direitos reservados.
