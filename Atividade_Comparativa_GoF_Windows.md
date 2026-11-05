# Laboratório de Engenharia de Software: três padrões GoF, um problema

**Atividade prática, comparativa e investigativa — Windows, Java e VS Code**

| Informações | Descrição |
|---|---|
| Tema | Factory Method × Strategy × Template Method |
| Nível | Intermediário |
| Duração estimada | 180 minutos (ajustável para duas aulas) |
| Ambiente | Windows 10/11, PowerShell, Visual Studio Code e JDK 17 ou superior |
| Entrega | Códigos executáveis, registro de testes e relatório comparativo em Markdown |
| Material-base | *Padrões GoF — Mesmo problema, três abordagens*, páginas 1–6, disponibilizado na disciplina |

> **Questão orientadora:** se três programas produzem a mesma saída, por que suas arquiteturas são diferentes — e em que circunstâncias cada uma é mais adequada?

## 1. Objetivos de aprendizagem

Ao concluir o laboratório, o estudante deverá ser capaz de:

1. Identificar **problema, variação e ponto de extensão** em uma arquitetura orientada a objetos.
2. Implementar o contrato `RegistrarClienteDecoder` e compreender **polimorfismo**, **interface**, **herança** e **composição**.
3. Demonstrar **Factory Method** pelo método de criação `criarDecoder()`.
4. Demonstrar **Strategy** pela substituição de um algoritmo sem alterar o serviço consumidor.
5. Demonstrar **Template Method** pela preservação de um fluxo com o método `final importar()` e pela substituição de uma etapa abstrata.
6. Compilar, executar, depurar e testar os programas no Windows.
7. Comparar **acoplamento, extensibilidade, responsabilidade e momento de escolha** dos três padrões.

### 1.1. Resultados esperados

Ao final, haverá **três programas Java executáveis e um quarto programa comparador**. Os três programas deverão converter a mesma informação, recebida em formatos diferentes, para uma representação idêntica:

```text
MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
```

O documento fornecido ilustra justamente esta arquitetura: **XML, CSV e texto fixo → `MsgRegistrarCliente`** (página 1); o contrato `RegistrarClienteDecoder` e seus decoders (página 2); as implementações de Factory Method, Strategy e Template Method (páginas 3 a 5); e a comparação final (página 6).

**Importante sobre a atividade:** os trechos do material-base são diagramas e excertos essenciais. Os códigos abaixo são **implementações didáticas completas em Java 17**, elaboradas para permitir compilação, execução e comparação real; não são transcrição literal dos fragmentos do PDF.

---

## 2. O problema único: cadastro a partir de três formatos

Uma aplicação recebe o cadastro de um cliente por três canais legados. Cada canal utiliza uma representação textual distinta, mas o serviço de cadastro precisa receber sempre o mesmo tipo de objeto.

| Formato | Exemplo simplificado | Regra de leitura |
|---|---|---|
| XML | `<cliente><nome>...</nome>...</cliente>` | Identificar valores por tags |
| CSV | `Ana Silva,12345678901,ana@exemplo.com` | Separar três campos por vírgula |
| Texto fixo | `Ana Silva           12345678901ana@exemplo.com...` | Separar por posições predeterminadas |

**Contrato de saída:** `MsgRegistrarCliente(nome, cpf, email)`.

### 2.1. O mesmo dado em três codificações

**XML**

```xml
<cliente>
  <nome>Ana Silva</nome>
  <cpf>12345678901</cpf>
  <email>ana@exemplo.com</email>
</cliente>
```

**CSV**

```text
Ana Silva,12345678901,ana@exemplo.com
```

**Texto fixo**

| Campo | Índices no Java | Largura |
|---|---:|---:|
| Nome | `substring(0, 20)` | 20 caracteres |
| CPF | `substring(20, 31)` | 11 caracteres |
| E-mail | `substring(31, 61)` | 30 caracteres |
| **Total** | Índices de `0` a `60` | **61 caracteres** |

**Atenção:** em Java, `substring(início, fim)` inclui o índice inicial e exclui o final. Assim, `substring(20, 31)` seleciona exatamente 11 caracteres. No texto fixo, espaços também são dados: não remova os espaços antes de recortar as posições.

O dado XML, CSV e de texto fixo será **sintético e exclusivo para exercício acadêmico**. O algoritmo confere que o CPF tem 11 dígitos, mas **não valida os dígitos verificadores do CPF**. A validação do e-mail também é deliberadamente simplificada. O CSV desse laboratório não implementa campos entre aspas ou vírgulas escapadas; em produção, utilize bibliotecas especializadas para CSV.

### 2.2. O que realmente será comparado?

Os métodos de leitura do XML, CSV e texto fixo são compartilhados para manter constante o problema. A variável experimental será **a arquitetura empregada para decidir como executar a importação**.

```text
                    ENTRADA (XML / CSV / TEXTO FIXO)
                                    |
                +-------------------+------------------+
                |                   |                  |
         FACTORY METHOD          STRATEGY       TEMPLATE METHOD
         quem cria?             qual usar?        qual etapa varia?
                |                   |                  |
                +-------------------+------------------+
                                    |
                         MsgRegistrarCliente
```

---

## 3. Preparação do ambiente no Windows (20 minutos)

### Passo 1 — Verificar a instalação do Java

1. Abra o menu Iniciar do Windows.
2. Pesquise **PowerShell** e abra o **Windows PowerShell** ou o **PowerShell**.
3. Digite os comandos, um por vez:

```powershell
java -version
javac -version
```

**O que fazem?** `java` é o executador de bytecode; `javac` é o compilador que transforma `.java` em `.class`. O exercício exige os **dois comandos**: ter somente um runtime não é suficiente.

**Resultado esperado:** ambas as versões são exibidas e são 17 ou superiores. Versões exatas podem variar; por exemplo, `javac 17.0.x` ou `javac 21.0.x` são adequadas.

**Se o sistema não reconhecer os comandos:** instale um **JDK 17 ou superior** por meio de uma distribuição confiável, como [Eclipse Temurin](https://adoptium.net/temurin/releases/?version=17). Feche e reabra o PowerShell após a instalação. Se ainda houver erro, confira se a pasta `bin` do JDK está disponível na variável de ambiente `Path`. Não é necessário instalar bibliotecas externas para esta atividade.

### Passo 2 — Preparar o Visual Studio Code

1. Instale o [Visual Studio Code](https://code.visualstudio.com/), caso ainda não esteja disponível.
2. Abra **Extensions** (atalho `Ctrl+Shift+X`).
3. Procure **Extension Pack for Java**, da Microsoft, e instale.
4. Se o VS Code solicitar o JDK, escolha a instalação do Java 17 ou superior.
5. Utilize o **Terminal → New Terminal** para abrir o terminal integrado, de preferência no perfil **PowerShell**.

**Por que usar a extensão?** Além de destacar a sintaxe, ela oferece navegação entre classes, execução e depuração com breakpoints, observação de variáveis e pilha de chamadas.

### Passo 3 — Criar as pastas no PowerShell

Copie e execute o bloco inteiro:

```powershell
Set-Location $HOME
New-Item -ItemType Directory -Path '.\comparacao-gof\src' -Force | Out-Null
New-Item -ItemType Directory -Path '.\comparacao-gof\bin' -Force | Out-Null
Set-Location '.\comparacao-gof'
Get-Location
```

**O que acontece?** `Set-Location` navega entre pastas; `New-Item` cria o diretório; `-Force` permite repetir o comando sem falhar caso a pasta já exista; `Out-Null` oculta informações de criação; `Get-Location` confirma a pasta atual.

A estrutura que será construída:

```text
comparacao-gof/
├── src/
│   ├── BaseComum.java
│   ├── FactoryMethodDemo.java
│   ├── StrategyDemo.java
│   ├── TemplateMethodDemo.java
│   └── ComparacaoDemo.java
├── bin/                      ← arquivos .class após compilar
└── resultado-testes.txt      ← gerado somente na etapa de evidências
```

### Passo 4 — Abrir a pasta no editor

No terminal em `comparacao-gof`, digite:

```powershell
code .
```

O comando `code .` abre a pasta atual no VS Code. **Se `code` não for reconhecido**, abra o VS Code manualmente e escolha **File → Open Folder**, selecionando `comparacao-gof` dentro da pasta do seu usuário. O restante do exercício será o mesmo.

### Ponto de verificação A

- [ ] `java -version` apresentou uma versão compatível.
- [ ] `javac -version` funcionou.
- [ ] A pasta `src` aparece no Explorer do VS Code.
- [ ] A pasta `bin` foi criada e está inicialmente vazia.
- [ ] O terminal integrado está aberto na raiz `comparacao-gof`.

---

## 4. Etapa I — Construção do domínio comum (25 minutos)

**Pergunta desta etapa:** como garantir que estamos comparando padrões diferentes, e não regras de negócio diferentes?

A resposta é manter **iguais os formatos de entrada, os métodos de leitura, a validação simplificada e o objeto de saída**.

### Passo 5 — Criar a base reutilizável

No painel Explorer do VS Code, clique com o botão direito em `src`, escolha **New File** e nomeie o arquivo `BaseComum.java`.

O arquivo conterá:

- `DadosCliente`: valores extraídos do documento de entrada.
- `MsgRegistrarCliente`: mensagem padronizada de cadastro.
- `RegistrarClienteDecoder`: contrato comum com `decode(String)`.
- `EntradasExemplo`: dados de teste equivalentes em XML, CSV e texto fixo.
- `LeitoresDados`: leitura e separação de campos conforme o formato.
- `Mensagens`: construção do objeto final com verificações básicas.
- Três decoders concretos, um para cada formato.

**Por que dois `record`s?** `DadosCliente` representa a extração intermediária. `MsgRegistrarCliente` representa o resultado que os consumidores esperam. A separação permite identificar claramente o instante em que os campos extraídos se transformam na mensagem de domínio.

### Código completo — `src/BaseComum.java`

> Crie o arquivo com exatamente esse nome na pasta `src` e copie todo o bloco. Não acrescente ``` ao código Java.

```java
import java.io.IOException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Os records representam dados imutaveis (Java 17).
record DadosCliente(String nome, String cpf, String email) {}
record MsgRegistrarCliente(String nome, String cpf, String email) {}

// Contrato reutilizado por Factory Method e Strategy.
interface RegistrarClienteDecoder {
    MsgRegistrarCliente decode(String dados);
}

final class EntradasExemplo {
    private EntradasExemplo() {}

    static String xml() {
        return "<cliente><nome>Ana Silva</nome><cpf>12345678901</cpf>"
             + "<email>ana@exemplo.com</email></cliente>";
    }

    static String csv() {
        return "Ana Silva,12345678901,ana@exemplo.com";
    }

    static String textoFixo() {
        return String.format("%-20s%-11s%-30s",
                "Ana Silva", "12345678901", "ana@exemplo.com");
    }
}

final class LeitoresDados {
    private LeitoresDados() {}

    static DadosCliente xml(String entrada) {
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            Document documento = fabrica.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(entrada)));
            Element raiz = documento.getDocumentElement();
            if (!"cliente".equals(raiz.getTagName())) {
                throw new IllegalArgumentException("A raiz XML deve ser <cliente>.");
            }
            return new DadosCliente(
                    lerTag(raiz, "nome"),
                    lerTag(raiz, "cpf"),
                    lerTag(raiz, "email"));
        } catch (ParserConfigurationException | SAXException | IOException erro) {
            throw new IllegalArgumentException("XML invalido: " + erro.getMessage(), erro);
        }
    }

    private static String lerTag(Element raiz, String tag) {
        var elementos = raiz.getElementsByTagName(tag);
        if (elementos.getLength() != 1) {
            throw new IllegalArgumentException("O XML deve conter uma tag <" + tag + ">.");
        }
        return elementos.item(0).getTextContent().strip();
    }

    static DadosCliente csv(String entrada) {
        // CSV simplificado: campos separados por virgula, sem aspas ou virgulas internas.
        String[] campos = entrada.split(",", -1);
        if (campos.length != 3) {
            throw new IllegalArgumentException("O CSV deve conter exatamente 3 campos.");
        }
        return new DadosCliente(
                campos[0].strip(), campos[1].strip(), campos[2].strip());
    }

    static DadosCliente textoFixo(String entrada) {
        // Layout: nome [0..19], CPF [20..30], e-mail [31..60].
        if (entrada.length() != 61) {
            throw new IllegalArgumentException("O texto fixo deve ter 61 caracteres.");
        }
        return new DadosCliente(
                entrada.substring(0, 20).strip(),
                entrada.substring(20, 31).strip(),
                entrada.substring(31, 61).strip());
    }
}

final class Mensagens {
    private Mensagens() {}

    static MsgRegistrarCliente montar(DadosCliente dados) {
        if (dados == null || dados.nome().isBlank()) {
            throw new IllegalArgumentException("O nome nao pode ficar vazio.");
        }
        if (!dados.cpf().matches("\\d{11}")) {
            throw new IllegalArgumentException("O CPF deve ter 11 digitos.");
        }
        // Verificacao didatica de formato, nao validacao completa de e-mail.
        if (!dados.email().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("O e-mail apresenta formato invalido.");
        }
        return new MsgRegistrarCliente(dados.nome(), dados.cpf(), dados.email());
    }
}

final class RegistrarClienteXMLDecoder implements RegistrarClienteDecoder {
    @Override
    public MsgRegistrarCliente decode(String dados) {
        return Mensagens.montar(LeitoresDados.xml(dados));
    }
}

final class RegistrarClienteCSVDecoder implements RegistrarClienteDecoder {
    @Override
    public MsgRegistrarCliente decode(String dados) {
        return Mensagens.montar(LeitoresDados.csv(dados));
    }
}

final class RegistrarClienteTextoFixoDecoder implements RegistrarClienteDecoder {
    @Override
    public MsgRegistrarCliente decode(String dados) {
        return Mensagens.montar(LeitoresDados.textoFixo(dados));
    }
}
```

### Passo 6 — Interpretar o contrato e os decoders

Observe estas responsabilidades no código recém-criado:

| Elemento | Responsabilidade | Consequência arquitetural |
|---|---|---|
| `RegistrarClienteDecoder` | Define `decode(String)` | O consumidor pode trabalhar com a interface |
| `RegistrarClienteXMLDecoder` | Extrai o XML e gera a mensagem | O XML fica isolado em uma implementação |
| `RegistrarClienteCSVDecoder` | Extrai o CSV e gera a mensagem | O CSV fica isolado em outra implementação |
| `RegistrarClienteTextoFixoDecoder` | Usa larguras fixas e gera a mensagem | Os índices pertencem ao leitor especializado |
| `Mensagens.montar` | Aplica validação simplificada e cria o objeto | Os três formatos seguem as mesmas regras de saída |

**Nota técnica sobre XML:** o programa utiliza o parser XML do próprio JDK e rejeita declarações `DOCTYPE`, evitando a habilitação de recursos de entidades externas neste exemplo. Essa configuração aparece na infraestrutura de leitura; **não define nenhum dos três padrões GoF**.

**Nota técnica sobre polimorfismo:** uma variável declarada como `RegistrarClienteDecoder` pode referenciar qualquer classe que implemente a interface. Qual implementação utilizar e **como ela é selecionada** é precisamente o foco das próximas etapas.

### Passo 7 — Compilar apenas a base

No terminal PowerShell, na raiz `comparacao-gof`, execute:

```powershell
javac -encoding UTF-8 -d .\bin .\src\BaseComum.java
```

**Entenda os parâmetros:** `-encoding UTF-8` informa a codificação do código-fonte; `-d .\bin` direciona os arquivos compilados para `bin`; `.\src\BaseComum.java` identifica o arquivo a compilar.

Não há saída de console esperada em uma compilação bem-sucedida. Alguns arquivos `.class` devem aparecer na pasta `bin`. Ainda não existe um método `main()` nesta base; isso é intencional.

### Ponto de verificação B

- [ ] O arquivo `BaseComum.java` não mostra erros de compilação.
- [ ] Os arquivos `.class` surgiram em `bin`.
- [ ] Está clara a diferença entre `DadosCliente` e `MsgRegistrarCliente`.
- [ ] É possível localizar os três métodos de leitura dentro de `LeitoresDados`.

---

## 5. Etapa II — Factory Method: variar a criação (25 minutos)

**Questão central:** *qual decoder deve nascer?*

No esquema da página 3 do material-base, a classe abstrata `ImportacaoFactory` declara `criarDecoder()`. Cada factory concreta devolve um decoder adequado, e a operação `importar()` utiliza a interface abstrata, sem instanciar diretamente `RegistrarClienteXMLDecoder` ou os demais produtos.

### Passo 8 — Criar o programa do Factory Method

Crie o arquivo `src/FactoryMethodDemo.java` e insira:

### Código completo — `src/FactoryMethodDemo.java`

> Crie o arquivo com exatamente esse nome na pasta `src` e copie todo o bloco. Não acrescente ``` ao código Java.

```java
// Creator: define o metodo fabrica e a operacao que utiliza o produto.
abstract class ImportacaoFactory {
    public abstract RegistrarClienteDecoder criarDecoder();

    public MsgRegistrarCliente importar(String dados) {
        RegistrarClienteDecoder decoder = criarDecoder();
        return decoder.decode(dados);
    }
}

// Concrete Creators: cada subclasse decide qual decoder instanciar.
final class XMLFactory extends ImportacaoFactory {
    @Override
    public RegistrarClienteDecoder criarDecoder() {
        return new RegistrarClienteXMLDecoder();
    }
}

final class CSVFactory extends ImportacaoFactory {
    @Override
    public RegistrarClienteDecoder criarDecoder() {
        return new RegistrarClienteCSVDecoder();
    }
}

final class TextoFixoFactory extends ImportacaoFactory {
    @Override
    public RegistrarClienteDecoder criarDecoder() {
        return new RegistrarClienteTextoFixoDecoder();
    }
}

public class FactoryMethodDemo {
    public static void main(String[] args) {
        mostrar("XML", new XMLFactory(), EntradasExemplo.xml());
        mostrar("CSV", new CSVFactory(), EntradasExemplo.csv());
        mostrar("TEXTO FIXO", new TextoFixoFactory(), EntradasExemplo.textoFixo());
    }

    private static void mostrar(String formato, ImportacaoFactory fabrica, String entrada) {
        MsgRegistrarCliente resultado = fabrica.importar(entrada);
        System.out.println("Factory Method | " + formato + " | " + resultado);
    }
}
```

### Passo 9 — Examinar a execução

```text
FactoryMethodDemo.main()
       ↓
new XMLFactory()
       ↓
ImportacaoFactory.importar(dados)
       ↓
XMLFactory.criarDecoder()
       ↓
new RegistrarClienteXMLDecoder()
       ↓
decoder.decode(dados)
       ↓
MsgRegistrarCliente
```

**Três distinções importantes:**

1. `ImportacaoFactory` é o **Creator abstrato**: oferece a operação `importar()` e delega a criação a `criarDecoder()`.
2. `XMLFactory`, `CSVFactory` e `TextoFixoFactory` são os **Concrete Creators**: decidem o produto instanciado.
3. `RegistrarClienteDecoder` é o **Product abstrato**: a operação `importar()` depende da interface e não da classe concreta.

**Atenção conceitual:** possuir uma classe chamada `Factory` não é suficiente para caracterizar Factory Method. Aqui o ponto decisivo é a **sobrescrita polimórfica do método de criação** por subclasses do Creator.

### Passo 10 — Compilar e executar

```powershell
javac -encoding UTF-8 -d .\bin .\src\BaseComum.java .\src\FactoryMethodDemo.java
java -cp .\bin FactoryMethodDemo
```

`java -cp .\bin` define a pasta em que o Java procura as classes compiladas. `FactoryMethodDemo` é o nome da classe com `main`, **sem a extensão `.java`**.

**Saída esperada:**

```text
Factory Method | XML | MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
Factory Method | CSV | MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
Factory Method | TEXTO FIXO | MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
```

### Investigação 1 — Evidência de Factory Method

Responda no relatório:

1. Em qual **linha/método** aparece `new RegistrarClienteXMLDecoder()`?
2. O método `ImportacaoFactory.importar()` contém `new RegistrarClienteXMLDecoder()`? Qual vantagem existe nessa separação?
3. Se a importação for CSV, qual **classe decide o produto**?
4. A escolha de uma factory concreta ocorre antes ou depois da chamada a `criarDecoder()`?

> **Checkpoint arquitetural:** no Factory Method, a criação de um objeto é delegada a um método que subclasses especializam.

---

## 6. Etapa III — Strategy: variar o comportamento (25 minutos)

**Questão central:** *qual decoder o serviço deve usar agora?*

No esquema da página 4, as implementações de `RegistrarClienteDecoder` deixam de ser vistas prioritariamente como produtos a instanciar e passam a ser **estratégias intercambiáveis**. O consumidor é `CadastroClienteService`, que guarda a referência ao decoder recebido.

### Passo 11 — Criar o programa do Strategy

Crie `src/StrategyDemo.java` e insira:

### Código completo — `src/StrategyDemo.java`

> Crie o arquivo com exatamente esse nome na pasta `src` e copie todo o bloco. Não acrescente ``` ao código Java.

```java
import java.util.Objects;

// Context: recebe um algoritmo e pode substitui-lo em tempo de execucao.
final class CadastroClienteService {
    private RegistrarClienteDecoder decoder;

    public CadastroClienteService(RegistrarClienteDecoder decoder) {
        definirDecoder(decoder);
    }

    public void definirDecoder(RegistrarClienteDecoder novoDecoder) {
        this.decoder = Objects.requireNonNull(novoDecoder, "Decoder obrigatorio.");
    }

    public MsgRegistrarCliente importar(String dados) {
        return decoder.decode(dados);
    }
}

public class StrategyDemo {
    public static void main(String[] args) {
        CadastroClienteService servico =
                new CadastroClienteService(new RegistrarClienteXMLDecoder());

        System.out.println("Strategy | XML | " + servico.importar(EntradasExemplo.xml()));

        // O mesmo objeto servico passa a utilizar outra estrategia.
        servico.definirDecoder(new RegistrarClienteCSVDecoder());
        System.out.println("Strategy | CSV | " + servico.importar(EntradasExemplo.csv()));

        servico.definirDecoder(new RegistrarClienteTextoFixoDecoder());
        System.out.println("Strategy | TEXTO FIXO | "
                + servico.importar(EntradasExemplo.textoFixo()));
    }
}
```

### Passo 12 — Examinar a execução

```text
new CadastroClienteService(XMLDecoder)
                    |
       servico.importar(XML)
                    |
             XMLDecoder.decode
                    |
     servico.definirDecoder(CSVDecoder)
                    |
       servico.importar(CSV)
                    |
             CSVDecoder.decode
```

**O que é a estratégia?** Cada classe que implementa `RegistrarClienteDecoder` contém a maneira de interpretar uma entrada. **O que é o contexto?** O `CadastroClienteService`, pois mantém a referência à estratégia e delega a operação `importar()` ao objeto atual.

A **composição** aparece no atributo `private RegistrarClienteDecoder decoder`. Em vez de herdar um algoritmo concreto, o serviço *possui* uma dependência para uma interface e pode trocá-la.

**Momento marcante da atividade:** repare que o objeto `servico` é criado uma única vez. Chamadas a `definirDecoder(...)` alteram seu comportamento **durante a execução**, sem reconstruir o serviço nem reescrever `importar()`.

### Passo 13 — Compilar e executar

```powershell
javac -encoding UTF-8 -d .\bin .\src\BaseComum.java .\src\StrategyDemo.java
java -cp .\bin StrategyDemo
```

**Saída esperada:**

```text
Strategy | XML | MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
Strategy | CSV | MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
Strategy | TEXTO FIXO | MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
```

### Investigação 2 — Evidência de Strategy

1. Qual campo guarda a estratégia ativa?
2. Como o programa substitui XML por CSV sem criar um novo `CadastroClienteService`?
3. Qual instrução seria alterada para começar o sistema pelo decoder CSV?
4. O método `CadastroClienteService.importar()` conhece os detalhes de XML ou CSV? Justifique.
5. Por que a implementação é um exemplo de **composição com interface**?

> **Checkpoint arquitetural:** no Strategy, um comportamento variável é encapsulado em objetos substituíveis por um contexto.

---

## 7. Etapa IV — Template Method: variar uma etapa, preservar o fluxo (30 minutos)

**Questão central:** *quais passos permanecem fixos e quais passos podem ser personalizados?*

Na página 5 do material-base, o processo possui uma sequência conceitual: **validar → extrair → montar mensagem → retornar**. No programa, essas ações são implementadas em um método `final` que impede subclasses de alterar sua ordem.

### Passo 14 — Criar o programa do Template Method

Crie `src/TemplateMethodDemo.java` e insira:

### Código completo — `src/TemplateMethodDemo.java`

> Crie o arquivo com exatamente esse nome na pasta `src` e copie todo o bloco. Não acrescente ``` ao código Java.

```java
// Abstract Class: fixa a ordem do algoritmo; as subclasses mudam a extracao.
abstract class ImportadorClienteTemplate {
    public final MsgRegistrarCliente importar(String entrada) {
        System.out.println("  [1/3] Validar entrada");
        validarEntrada(entrada);

        System.out.println("  [2/3] Extrair campos (etapa variavel)");
        DadosCliente dados = extrairCampos(entrada);

        System.out.println("  [3/3] Montar mensagem (etapa comum)");
        return montarMensagem(dados);
    }

    protected void validarEntrada(String entrada) {
        if (entrada == null || entrada.isBlank()) {
            throw new IllegalArgumentException("A entrada nao pode ficar vazia.");
        }
    }

    protected abstract DadosCliente extrairCampos(String entrada);

    protected MsgRegistrarCliente montarMensagem(DadosCliente dados) {
        return Mensagens.montar(dados);
    }
}

final class ImportadorXML extends ImportadorClienteTemplate {
    @Override
    protected DadosCliente extrairCampos(String entrada) {
        return LeitoresDados.xml(entrada);
    }
}

final class ImportadorCSV extends ImportadorClienteTemplate {
    @Override
    protected DadosCliente extrairCampos(String entrada) {
        return LeitoresDados.csv(entrada);
    }
}

final class ImportadorTextoFixo extends ImportadorClienteTemplate {
    @Override
    protected DadosCliente extrairCampos(String entrada) {
        return LeitoresDados.textoFixo(entrada);
    }
}

public class TemplateMethodDemo {
    public static void main(String[] args) {
        executar("XML", new ImportadorXML(), EntradasExemplo.xml());
        executar("CSV", new ImportadorCSV(), EntradasExemplo.csv());
        executar("TEXTO FIXO", new ImportadorTextoFixo(), EntradasExemplo.textoFixo());
    }

    private static void executar(String formato, ImportadorClienteTemplate importador,
                                String entrada) {
        System.out.println("Template Method | " + formato);
        System.out.println("  Resultado: " + importador.importar(entrada));
    }
}
```

### Passo 15 — Localizar o algoritmo-base

O método-chave é:

```java
public final MsgRegistrarCliente importar(String entrada) {
    validarEntrada(entrada);
    DadosCliente dados = extrairCampos(entrada);
    return montarMensagem(dados);
}
```

No arquivo completo há também mensagens `System.out.println` para tornar a sequência observável, mas **elas não alteram a lógica**.

| Etapa | Método | Implementação |
|---|---|---|
| 1 | `validarEntrada` | Comum na superclasse |
| 2 | `extrairCampos` | Abstrato; cada subclasse implementa |
| 3 | `montarMensagem` | Comum na superclasse |
| 4 | `return` | Retorno da operação padronizada |

A palavra-chave `final` em `importar` impede que uma subclasse sobrescreva o algoritmo completo. A palavra-chave `abstract` em `extrairCampos` **obriga as subclasses concretas a oferecerem uma implementação**. Isso define um **ponto de extensão controlado**.

**Analogia de fixação:** uma linha de produção exige recepção, processamento e entrega em uma ordem definida. A máquina de processamento pode mudar segundo o produto; o procedimento geral da fábrica permanece.

### Passo 16 — Compilar e executar

```powershell
javac -encoding UTF-8 -d .\bin .\src\BaseComum.java .\src\TemplateMethodDemo.java
java -cp .\bin TemplateMethodDemo
```

**Trecho da saída esperada (XML):**

```text
Template Method | XML
  [1/3] Validar entrada
  [2/3] Extrair campos (etapa variavel)
  [3/3] Montar mensagem (etapa comum)
  Resultado: MsgRegistrarCliente[nome=Ana Silva, cpf=12345678901, email=ana@exemplo.com]
```

O mesmo conjunto de três etapas será exibido para CSV e texto fixo. A etapa `extrairCampos` escolherá um leitor diferente, mas a **sequência do algoritmo permanecerá inalterada**.

### Investigação 3 — Evidência de Template Method

1. Qual método é marcado como `final` e por quê?
2. Qual método é `abstract`? Onde cada subclasse o implementa?
3. Se a validação de entrada passar a ser obrigatória para todos os formatos, em quantas subclasses ela precisará ser duplicada?
4. O que diferencia **herança orientada ao fluxo** no Template Method de **composição de comportamentos** no Strategy?

### Experimento controlado — Demonstrar o efeito de `final`

Depois de registrar que o projeto funciona, **tente temporariamente** adicionar dentro da classe `ImportadorCSV`:

```java
// TENTATIVA INTENCIONALMENTE INVALIDA: NAO DEIXAR NO CODIGO FINAL.
@Override
public MsgRegistrarCliente importar(String entrada) {
    return null;
}
```

Compile o programa novamente. **Resultado esperado:** o compilador rejeitará a sobrescrita de `importar`, pois o método foi declarado `final` na superclasse. Esse erro é a evidência de que o fluxo não pode ser substituído por uma subclasse. **Remova esse trecho antes de continuar**, salvando o arquivo original novamente.

> **Checkpoint arquitetural:** o Template Method utiliza herança para preservar o algoritmo-base e permitir a substituição de passos específicos.

---

## 8. Etapa V — Comparação executável e testes (20 minutos)

Nos passos anteriores, as saídas pareciam iguais. Agora será realizado um teste **programático** para determinar se os objetos são realmente iguais.

### Passo 17 — Criar o comparador

Crie `src/ComparacaoDemo.java` e insira:

### Código completo — `src/ComparacaoDemo.java`

> Crie o arquivo com exatamente esse nome na pasta `src` e copie todo o bloco. Não acrescente ``` ao código Java.

```java
public class ComparacaoDemo {
    public static void main(String[] args) {
        comparar("XML", EntradasExemplo.xml(), new XMLFactory(),
                new RegistrarClienteXMLDecoder(), new ImportadorXML());
        comparar("CSV", EntradasExemplo.csv(), new CSVFactory(),
                new RegistrarClienteCSVDecoder(), new ImportadorCSV());
        comparar("TEXTO FIXO", EntradasExemplo.textoFixo(), new TextoFixoFactory(),
                new RegistrarClienteTextoFixoDecoder(), new ImportadorTextoFixo());

        esperarFalha("CSV com 2 campos",
                () -> new CSVFactory().importar("Ana Silva,12345678901"));
        esperarFalha("CPF com menos de 11 digitos",
                () -> new CadastroClienteService(new RegistrarClienteCSVDecoder())
                        .importar("Ana Silva,123,ana@exemplo.com"));
        esperarFalha("Texto fixo curto",
                () -> new ImportadorTextoFixo().importar("texto curto"));
        esperarFalha("Entrada vazia no Template Method",
                () -> new ImportadorXML().importar(""));

        System.out.println("TODOS OS TESTES PASSARAM.");
    }

    private static void comparar(String formato, String entrada,
                                 ImportacaoFactory fabrica,
                                 RegistrarClienteDecoder estrategia,
                                 ImportadorClienteTemplate template) {
        MsgRegistrarCliente resultadoFactory = fabrica.importar(entrada);
        MsgRegistrarCliente resultadoStrategy =
                new CadastroClienteService(estrategia).importar(entrada);
        MsgRegistrarCliente resultadoTemplate = template.importar(entrada);

        if (!resultadoFactory.equals(resultadoStrategy)
                || !resultadoFactory.equals(resultadoTemplate)) {
            throw new AssertionError("Divergencia na importacao: " + formato);
        }
        System.out.println("[OK] " + formato + ": os 3 padroes geraram o mesmo objeto.\n");
    }

    private static void esperarFalha(String cenario, Runnable teste) {
        try {
            teste.run();
            throw new AssertionError("A falha esperada nao ocorreu: " + cenario);
        } catch (IllegalArgumentException esperado) {
            System.out.println("[OK] Erro detectado: " + cenario
                    + " | motivo: " + esperado.getMessage());
        }
    }
}
```

### Passo 18 — Compilar os cinco arquivos

Na raiz do projeto, execute:

```powershell
javac -encoding UTF-8 -d .\bin .\src\BaseComum.java .\src\FactoryMethodDemo.java .\src\StrategyDemo.java .\src\TemplateMethodDemo.java .\src\ComparacaoDemo.java
```

A compilação de todos os arquivos na mesma invocação permite ao compilador resolver referências cruzadas entre classes. **Se aparecer algum erro, não avance:** o laboratório pressupõe código compilado antes do teste.

### Passo 19 — Executar a comparação

```powershell
java -cp .\bin ComparacaoDemo
```

**Saída esperada resumida:**

```text
[OK] XML: os 3 padroes geraram o mesmo objeto.
[OK] CSV: os 3 padroes geraram o mesmo objeto.
[OK] TEXTO FIXO: os 3 padroes geraram o mesmo objeto.
[OK] Erro detectado: CSV com 2 campos | motivo: O CSV deve conter exatamente 3 campos.
[OK] Erro detectado: CPF com menos de 11 digitos | motivo: O CPF deve ter 11 digitos.
[OK] Erro detectado: Texto fixo curto | motivo: O texto fixo deve ter 61 caracteres.
[OK] Erro detectado: Entrada vazia no Template Method | motivo: A entrada nao pode ficar vazia.
TODOS OS TESTES PASSARAM.
```

**Detalhe:** entre as linhas do resultado, o Template Method também imprime suas etapas `[1/3]`, `[2/3]` e `[3/3]`. A saída resumida acima omite somente essas linhas repetidas.

### Passo 20 — Entender o que foi efetivamente testado

A classe `ComparacaoDemo` verifica **dois grupos distintos**:

1. **Equivalência funcional**: para cada formato, Factory Method, Strategy e Template Method são executados e seus três `MsgRegistrarCliente` são comparados com `equals()`. Como `MsgRegistrarCliente` é um `record`, Java oferece comparação estrutural baseada nos componentes do objeto.
2. **Tratamento de erro**: quatro entradas inadequadas devem gerar `IllegalArgumentException`; o teste falha se um cenário indevido for aceito.

**Distinção importante:** o teste comprova a **equivalência de saída para os casos avaliados**, não a equivalência arquitetural e nem a ausência de todos os defeitos possíveis. Arquiteturas diferentes podem ser igualmente corretas para o mesmo conjunto de testes.

### Passo 21 — Salvar a evidência da execução

No PowerShell:

```powershell
java -cp .\bin ComparacaoDemo | Tee-Object -FilePath .\resultado-testes.txt
```

O `Tee-Object` exibe a saída no terminal **e** grava uma cópia em `resultado-testes.txt`. Guarde o arquivo como evidência da atividade. A codificação usada pelo PowerShell para esse arquivo pode variar conforme a versão, mas isso não afeta a execução dos testes.

### Quadro de casos de teste

| ID | Cenário | Entrada | Expectativa |
|---|---|---|---|
| T01 | XML válido | Documento XML do exemplo | Três mensagens equivalentes |
| T02 | CSV válido | Três campos separados por vírgula | Três mensagens equivalentes |
| T03 | Texto fixo válido | 61 caracteres no leiaute acordado | Três mensagens equivalentes |
| T04 | CSV incompleto | Dois campos | `IllegalArgumentException` |
| T05 | CPF com formato errado | Menos de 11 dígitos | `IllegalArgumentException` |
| T06 | Texto fixo truncado | Tamanho diferente de 61 | `IllegalArgumentException` |
| T07 | Entrada em branco | `""` no Template | `IllegalArgumentException` |

**Ponto de verificação C:** todos os casos devem terminar com a indicação `TODOS OS TESTES PASSARAM.`. Qualquer `AssertionError` significa que houve divergência ou ausência de erro esperado.

---

## 9. Depuração comparativa no VS Code (15 minutos)

Executar os programas é apenas a primeira camada da investigação. O depurador permite acompanhar a sequência real de chamadas e verificar em **qual objeto reside a decisão**.

### Passo 22 — Investigar o Factory Method

1. Abra `FactoryMethodDemo.java`.
2. Clique à esquerda do número da linha `return new RegistrarClienteXMLDecoder();` dentro de `XMLFactory` para criar um **breakpoint** (marcação vermelha).
3. Abra `FactoryMethodDemo.main()` e clique em **Run and Debug**, ou utilize o comando **Debug Java** disponível na extensão.
4. Quando a execução for interrompida, observe a classe concreta `XMLFactory`.
5. Use **Step Into (F11)** e **Step Over (F10)** conforme a necessidade.
6. Examine **Call Stack** e procure a chamada originada de `ImportacaoFactory.importar()`.

**O que procurar:** a delegação ao método de criação. A superclasse não possui o comando de instanciação da classe XML.

### Passo 23 — Investigar o Strategy

1. Abra `StrategyDemo.java`.
2. Crie breakpoint na linha `this.decoder = Objects.requireNonNull(...)`.
3. Execute `StrategyDemo` pelo depurador.
4. Observe o valor/tipo concreto de `novoDecoder` nas sucessivas chamadas a `definirDecoder`.
5. Observe que **o campo do mesmo objeto `servico` muda de referência**.

**O que procurar:** a escolha do algoritmo ocorre por **atribuição de dependência**, e não pela sobrescrita do fluxo de importação.

### Passo 24 — Investigar o Template Method

1. Abra `TemplateMethodDemo.java`.
2. Crie breakpoint na linha `DadosCliente dados = extrairCampos(entrada);`.
3. Execute `TemplateMethodDemo` pelo depurador.
4. Observe o tipo concreto do objeto importador quando a execução entra em `extrairCampos`.
5. Volte a `importar()` e confirme que as três etapas ocorrem sempre na mesma ordem.

**O que procurar:** a operação `importar()` mantém o controle do fluxo, enquanto as subclasses implementam **uma etapa interna**.

**Nota:** atalhos e opções de debug podem variar conforme a distribuição da extensão Java. Se a aplicação for iniciada com **Run Java** em vez de **Debug Java**, os breakpoints poderão ser ignorados.

---

## 10. Matriz de comparação técnica (10 minutos)

Preencha as últimas duas colunas com **evidências do código**: nome da classe, do atributo ou do método que comprova a afirmação.

| Critério | Factory Method | Strategy | Template Method |
|---|---|---|---|
| Questão central | Qual objeto criar? | Qual algoritmo utilizar? | Quais etapas variam? |
| Dimensão de variação | Instanciação | Comportamento | Passos do fluxo |
| Principal técnica | Sobrescrita do método de fábrica | Composição e delegação | Herança + método template |
| Ponto de extensão | `criarDecoder()` | `RegistrarClienteDecoder` injetado | `extrairCampos()` |
| Quem decide? | Factory concreta | Código consumidor do serviço | Subclasse especializada |
| É possível trocar na mesma instância do consumidor? | A operação cria conforme a factory concreta | Sim: `definirDecoder()` | Não por substituição da subclasse do mesmo objeto |
| Fluxo imposto pela superclasse? | Operação `importar()` reutilizada | Não | Sim: `importar()` é `final` |
| Objeto principal | `ImportacaoFactory` | `CadastroClienteService` | `ImportadorClienteTemplate` |

**Não confunda momento de decisão com possibilidade técnica.** Uma aplicação pode escolher **qual factory concreta utilizar** em tempo de execução. Da mesma forma, um objeto Strategy pode ser configurado no construtor e nunca mais ser trocado. O contraste didático é a **responsabilidade que cada padrão encapsula**, não uma suposta proibição universal de decisões dinâmicas.

### Perguntas de análise para o relatório

1. Qual dos três padrões melhor representa a necessidade de **trocar o formato de importação sem reconstruir o serviço consumidor**? Defenda com trechos do código.
2. Em uma biblioteca, diferentes subclasses precisam controlar a **instanciação de um decoder**. Qual padrão é mais diretamente indicado?
3. Um processo de auditoria exige sempre **validar → extrair → montar**, sem permitir que subclasses alterem a sequência. Qual padrão oferece controle explícito dessa exigência?
4. Onde há **herança**? Onde há **composição**? Os três padrões eliminam totalmente o acoplamento?
5. Um projeto pode combinar Factory Method e Strategy? Descreva uma situação plausível. Evite tratar os padrões como mutuamente exclusivos.
6. Qual padrão tende a exigir mais subclasses de um **Creator**? Qual requer uma interface de comportamento? Qual concentra o fluxo em uma classe abstrata?
7. Todos produzem o mesmo resultado: a escolha do padrão deve ser feita apenas por desempenho de execução? Quais critérios de manutenção devem orientar a decisão?

---

## 11. Desafio de extensão — quarto formato PSV (25 minutos extras ou tarefa)

**Cenário:** o cliente agora também pode chegar em PSV (*pipe-separated values*), com três campos separados pelo caractere `|`.

```text
Ana Silva|12345678901|ana@exemplo.com
```

**Objetivo:** adicionar o formato sem modificar os contratos centrais dos padrões e explicar **onde a nova variação entrou**.

### Passo 25 — Criar o leitor PSV

Em `BaseComum.java`, acrescente um método `psv(String entrada)` à classe `LeitoresDados`. Como pista, observe o padrão de `csv(...)` e o uso de `split(...)`.

**Cuidado técnico:** em expressões regulares, a barra vertical tem significado especial. Para tratá-la literalmente no Java, utilize `split("\\|", -1)` no **código-fonte** (isto é, a string Java contém duas barras invertidas antes de `|`).

Também acrescente a `EntradasExemplo` um método que devolva a string PSV.

### Passo 26 — Expandir Factory Method

- Crie um decoder que implemente `RegistrarClienteDecoder` e utilize `LeitoresDados.psv(...)`.
- Crie `PSVFactory extends ImportacaoFactory`, sobrescrevendo `criarDecoder()`.
- Acrescente uma chamada no `main()` de `FactoryMethodDemo`.

**Pergunta:** foi necessário modificar a implementação de `ImportacaoFactory.importar()`? Por quê?

### Passo 27 — Expandir Strategy

- Reutilize o decoder PSV da etapa anterior.
- Em `StrategyDemo`, chame `servico.definirDecoder(...)` passando o decoder PSV.
- Execute a importação de dados PSV no mesmo serviço.

**Pergunta:** foi necessário modificar a classe `CadastroClienteService`?

### Passo 28 — Expandir Template Method

- Crie `ImportadorPSV extends ImportadorClienteTemplate`.
- Implemente somente `extrairCampos(...)` usando `LeitoresDados.psv(...)`.
- Adicione uma chamada no `main()` de `TemplateMethodDemo`.

**Pergunta:** foi necessário alterar `ImportadorClienteTemplate.importar()`?

### Passo 29 — Expandir o teste comparativo

Acrescente um caso em `ComparacaoDemo` para comparar os três padrões com PSV. O resultado final deve manter a equivalência do `MsgRegistrarCliente`.

**Observação metodológica:** implementar uma rotina comum `LeitoresDados.psv(...)` é alteração de infraestrutura de parsing, compartilhada pelos três programas; isso não significa que o mecanismo de extensão dos padrões seja idêntico.

**Entrega do desafio:** apresente um quadro com o número de classes novas e de classes existentes alteradas em cada abordagem. Não conte os `main()` e o comparador como pontos internos de extensão; registre-os separadamente como ajustes de demonstração/teste.

---

## 12. Relatório individual de análise

Crie na raiz do projeto o arquivo `RELATORIO.md` com a estrutura abaixo e complete com **respostas autorais, nomes de métodos e evidências observadas**.

```markdown
# Relatório — Factory Method, Strategy e Template Method

**Discente:**
**Turma:**
**Data:**

## 1. Ambiente
- Windows:
- JDK:
- VS Code:

## 2. Evidência das execuções
- Factory Method:
- Strategy:
- Template Method:
- Comparador e testes:

## 3. Evidências dos pontos de extensão
- Factory Method — método e classe:
- Strategy — atributo, injeção e substituição:
- Template Method — método final e método abstrato:

## 4. Matriz comparativa
- Criação:
- Comportamento:
- Sequência de etapas:

## 5. Análise crítica
- Qual arquitetura adotar em cada cenário e por quê?
- Diferenças observadas na depuração:
- Uma vantagem e uma limitação de cada padrão:

## 6. Desafio PSV (se realizado)
- Classes criadas:
- Classes alteradas:
- Resultados dos testes:

## 7. Conclusão
- Qual foi a diferença de projeto mais importante observada?
```

### Critérios de avaliação sugeridos (100 pontos)

| Critério | Pontos | Evidência requerida |
|---|---:|---|
| Ambiente e compilação | 10 | JDK funcional, arquivos organizados e sem erros |
| Factory Method | 15 | Método de fábrica e três creators concretos funcionando |
| Strategy | 15 | Mesmo serviço com substituição de três estratégias |
| Template Method | 15 | Algoritmo `final` e extração variável funcionando |
| Testes | 20 | Equivalência em três formatos e entradas inválidas detectadas |
| Comparação argumentada | 15 | Quadro e respostas justificadas por código |
| Qualidade do relatório | 10 | Clareza, evidências, terminologia e conclusão |

**Desafio PSV:** pode ser utilizado como bonificação ou atividade complementar, conforme os critérios definidos para a turma.

---

## 13. Problemas comuns no Windows e correções

| Sintoma | Possível causa | Verificação/correção |
|---|---|---|
| `javac` não é reconhecido | JDK não instalado ou `Path` não atualizado | Verifique `javac -version`, reinstale/configure o JDK e reabra o terminal |
| `java` funciona, `javac` não | Somente runtime instalado ou ambiente configurado incorretamente | Instale um **JDK**, não apenas um runtime |
| `error: file not found: src\\...` | Terminal fora de `comparacao-gof` ou nome incorreto | Rode `Get-Location` e `Get-ChildItem .\\src` |
| `Could not find or load main class` | Classe não compilada, nome incorreto ou classpath errado | Compile primeiro; execute `java -cp .\\bin NomeDaClasse` |
| `class X is public, should be declared in a file named X.java` | Nome do arquivo difere da classe pública | Corrija o nome do arquivo conforme o roteiro |
| `cannot find symbol` | Referência ausente, arquivo não salvo ou compilação incompleta | Salve todos e compile as dependências necessárias |
| Caracteres estranhos no terminal | Codificação de console incompatível | Compile com `-encoding UTF-8`; se necessário, execute `chcp 65001` e reabra o terminal |
| Erro ao recortar texto fixo | String truncada ou espaços removidos | Gere com `String.format` e preserve os 61 caracteres |
| O teste falha após o experimento com `final` | Código inválido não removido | Retire a sobrescrita temporária e recompile |
| Debug não para no breakpoint | Aplicação executada sem modo de depuração | Utilize **Debug Java**, não somente **Run Java** |

**Comandos úteis para diagnóstico:**

```powershell
Get-Location
Get-ChildItem .\src
Get-ChildItem .\bin
java -version
javac -version
```

**Regra de segurança:** não é necessário executar PowerShell ou VS Code como administrador, nem desabilitar a política de execução de scripts do Windows para esta atividade.

---

## 14. Síntese para revisão

| Pergunta de prova | Padrão | Evidência identificadora |
|---|---|---|
| **Quem decide qual objeto será instanciado?** | Factory Method | `XMLFactory.criarDecoder()` |
| **Quem permite alternar a implementação de um algoritmo?** | Strategy | `servico.definirDecoder(...)` |
| **Quem fixa a ordem e libera algumas etapas para subclasses?** | Template Method | `final importar()` + `abstract extrairCampos()` |

> **Princípio de escolha:** não se seleciona um padrão pela aparência das classes ou pelo fato de gerar uma mesma saída. Identifica-se primeiro a **dimensão real da mudança**: criação, comportamento ou organização das etapas.

**Critério de conclusão do laboratório:** o estudante deverá ser capaz de apontar, em seu próprio código, **o método onde nasce um objeto (Factory Method), o atributo onde um comportamento é trocado (Strategy) e o método que impõe uma sequência (Template Method)**.

### Referências de apoio

- Material disponibilizado na aula: *Padrões GoF — Mesmo problema, três abordagens*, 6 páginas.
- Gamma, E.; Helm, R.; Johnson, R.; Vlissides, J. *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley, 1994.
- Documentação Java: [Classes e interfaces](https://dev.java/learn/classes-objects/), [Records](https://dev.java/learn/records/) e [Java API](https://docs.oracle.com/en/java/javase/17/docs/api/).

---

**Disciplina:** Engenharia de Software / Padrões de Projeto  
**Material para execução e análise pelos estudantes — Laboratório em Windows**

