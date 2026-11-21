# SACM - Sistema de Agendamento de Consultas Médicas
 
Avaliação do 2º bimestre de Programação Orientada a Objetos.
Aplicação de console em Java com Spring Boot e Spring JDBC (`JdbcTemplate`), com persistência em MySQL.
 
- Autor: Felipe Piva
- Repositório: https://github.com/Felipepiv/Prova-POO (projeto Maven na pasta `sacm`)
## Requisitos
 
| Item | Versão |
|---|---|
| Java (JDK) | 21 |
| MySQL Server | 8.x |
| Maven | não precisa instalar, o projeto usa o Maven Wrapper (`mvnw`) |
| MySQL Workbench | opcional, para rodar o script e conferir os dados |
 
## Estrutura
 
```
sacm/
├── database/schema.sql              script do banco (entregue com o projeto)
├── pom.xml
└── src/main
    ├── java/br/edu/avaliacao/sacm
    │   ├── SacmApplication.java
    │   ├── model/        Pessoa (abstrata), Medico, Paciente, Consulta
    │   ├── enums/        StatusPessoa, DisponibilidadeMedico, Especialidade, StatusConsulta
    │   ├── interfaces/   Agendavel
    │   ├── repository/   MedicoRepository, PacienteRepository, ConsultaRepository
    │   ├── service/      ConsultaService
    │   ├── exception/    RegraNegocioException, ConflitoHorarioException
    │   └── console/      SistemaConsole
    └── resources/application.properties
```
 
## 1. Criar o banco de dados
 
Execute o script `database/schema.sql` no MySQL. Pelo Workbench: `File > Open SQL Script`, escolha o arquivo, selecione tudo (`Ctrl+A`) e execute com o primeiro raio (`Ctrl+Shift+Enter`). Pelo terminal:
 
```
mysql -u root -p < database/schema.sql
```
 
Confira:
 
```sql
USE sacm_db;
SHOW TABLES;   -- deve listar consulta, medico e paciente
```
 
## 2. Criar um usuário com acesso restrito ao schema
 
Como `root`, troque `SuaSenhaAqui` por uma senha sua:
 
```sql
CREATE USER 'sacm_user'@'localhost' IDENTIFIED BY 'SuaSenhaAqui';
GRANT SELECT, INSERT, UPDATE, DELETE ON sacm_db.* TO 'sacm_user'@'localhost';
FLUSH PRIVILEGES;
```
 
No Workbench, execute os três comandos juntos (selecione tudo antes de clicar no raio). Se executar só uma linha, o usuário não é criado por completo.
 
## 3. Configurar as credenciais (variáveis de ambiente)
 
O `application.properties` lê o usuário e a senha de variáveis de ambiente. **Não escreva senhas em arquivos do projeto nem no Git.**
 
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sacm_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```
 
Defina as variáveis **no mesmo terminal em que vai rodar o projeto** (elas valem só para aquele terminal):
 
**Prompt de Comando (CMD)**, sem aspas e sem espaço antes ou depois do `=`:
 
```
set DB_USER=sacm_user
set DB_PASSWORD=SuaSenhaAqui
```
 
**PowerShell:**
 
```powershell
$env:DB_USER = "sacm_user"
$env:DB_PASSWORD = "SuaSenhaAqui"
```
 
**Linux / macOS:**
 
```bash
export DB_USER=sacm_user
export DB_PASSWORD=SuaSenhaAqui
```
 
## 4. Compilar e executar
 
Abra o terminal **dentro da pasta `sacm`** (a que contém o `pom.xml`), defina as variáveis do passo 3 e rode:
 
| Sistema | Compilar | Executar |
|---|---|---|
| CMD | `mvnw.cmd clean compile` | `mvnw.cmd spring-boot:run` |
| PowerShell | `.\mvnw.cmd clean compile` | `.\mvnw.cmd spring-boot:run` |
| Linux / macOS | `./mvnw clean compile` | `./mvnw spring-boot:run` |
 
Também dá para executar a classe `SacmApplication` pelo VS Code. A primeira execução baixa as dependências e pode demorar alguns minutos. Quando o menu aparecer, o sistema está conectado ao banco:
 
```
=== SACM ===
1 - Cadastrar médico
2 - Cadastrar paciente
3 - Agendar consulta
4 - Listar consultas
5 - Alterar status de consulta
0 - Sair
```
 
## 5. Como usar
 
- Datas de nascimento: `dd/MM/yyyy` (exemplo `10/03/1985`).
- Data e hora da consulta: `dd/MM/yyyy HH:mm` (exemplo `25/11/2026 14:00`), sempre no futuro.
- Especialidades aceitas: `CLINICO_GERAL`, `CARDIOLOGIA`, `DERMATOLOGIA`, `PEDIATRIA`, `ORTOPEDIA`.
- Ao cadastrar, o sistema mostra o id gerado. Use esse id para agendar.
- Status da consulta: `AGENDADA` → `CONFIRMADA` ou `CANCELADA`; `CONFIRMADA` → `FINALIZADA` ou `CANCELADA`. `CANCELADA` e `FINALIZADA` não mudam mais.
### Roteiro de demonstração
 
1. **Cadastro:** opção 1 (médico, que nasce ATIVO e DISPONIVEL) e opção 2 (paciente, que nasce ATIVO).
2. **Agendamento válido:** opção 3 com médico 1, paciente 1 e data futura.
3. **Conflito:** repita o mesmo agendamento. Esperado: `Erro: Médico ou paciente já possui consulta neste horário`.
4. **Cancelamento:** opção 5, consulta 1, ação 2.
5. **Novo agendamento no horário liberado:** repita o passo 2. Agora é aceito.
6. **Persistência:** feche com a opção 0, abra de novo e confira no MySQL:
```sql
SELECT id, nome, status, disponibilidade FROM medico;
SELECT id, nome, status FROM paciente;
SELECT id, id_medico, id_paciente, data_hora, status FROM consulta;
```
 
## 6. Regras de negócio implementadas
 
| Regra | Onde |
|---|---|
| RN01 CRM e CPF únicos | repositórios (`existePorCrm`, `existePorCpf`) e `SistemaConsole.validarCpfUnico` |
| RN02 médico e paciente existem e estão ativos | `ConsultaService.agendarConsulta` |
| RN03 médico DISPONIVEL | `ConsultaService.agendarConsulta`, `Medico.isDisponivel` |
| RN04 sem conflito de horário | `ConsultaService.verificarConflitoHorario` |
| RN05 data futura e motivo obrigatório | `ConsultaService.agendarConsulta` |
| RN06 transições de status | `Consulta.confirmar`, `cancelar`, `finalizar` |
| RN07 cancelamento e confirmação persistidos | `ConsultaRepository.salvar` (UPDATE) |
| RN08 persistência após reiniciar | MySQL via `JdbcTemplate` |
| RN09 erros claros e causa técnica | `RegraNegocioException(mensagem, causa)`; o menu imprime `Causa técnica:` |
| RN10 constantes `static final` | `SistemaConsole.FORMATO_DATA` e `FORMATO_DATA_HORA` |
 
## 7. Problemas comuns
 
O menu mostra a causa técnica quando o banco falha. Use a **última** linha `Causa técnica:`:
 
| Mensagem | Causa e solução |
|---|---|
| `Access denied for user 'sacm_user'` | A senha em `DB_PASSWORD` é diferente da do usuário no MySQL, ou as variáveis não foram definidas nesse terminal. Redefina com `set` e rode de novo. |
| `Public Key Retrieval is not allowed` | A URL precisa de `allowPublicKeyRetrieval=true` (já está no `application.properties`). |
| `Unknown database 'sacm_db'` | O `schema.sql` não foi executado. |
| `Table '...' doesn't exist` | As tabelas não foram criadas; execute o script inteiro. |
| `Communications link failure` | O MySQL está parado. No Windows, inicie o serviço **MySQL80**. |
| `'.\mvnw.cmd' não é reconhecido` | O terminal está na pasta errada. Entre na pasta `sacm`. No CMD use `mvnw.cmd` sem `.\`. |
| `package ... does not match the expected package` | Os arquivos devem ficar em `src/main/java/br/edu/avaliacao/sacm/...`. |
 
## 8. Observações
 
- `useSSL=false` e `allowPublicKeyRetrieval=true` são adequados para ambiente local de estudo; em produção seria necessário usar SSL.
- Não versionar `target/` nem senhas (o `.gitignore` já ignora `target/`).
- Limitações conhecidas: não há UPDATE de médico e paciente (métodos como `alterarDisponibilidade` e `atualizarConvenio` alteram só o objeto em memória), e a verificação de conflito carrega todas as consultas. Detalhes no relatório técnico.