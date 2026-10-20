
public class cliente {
    public static void main(String[] args) {
        String linha = "Ana Silva           12345678901ana@exemplo.com               ";

        String nome = linha.substring(0, 20).trim();
        String cpf = linha.substring(20, 31).trim();
        String email = linha.substring(31, 61).trim();

        System.out.println("Nome: " + nome);
        System.out.println("CPF: " + cpf);
        System.out.println("Email: " + email);
    }
}
