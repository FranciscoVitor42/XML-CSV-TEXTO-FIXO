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
