import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void enviaMensagem(){
        try{
            // Exibe instruções para o cliente
            System.out.println("Comandos disponíveis:");
            System.out.println("/upload <caminho_do_arquivo> - Para enviar um arquivo ao servidor");
            System.out.println("/download <nome_do_arquivo> - Para baixar um arquivo do servidor");
            System.out.println("Digite sua mensagem normalmente para enviá-la ao chat.");

            bufferedWriter.write(username); //
            bufferedWriter.newLine(); //
            bufferedWriter.flush(); //

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String mensagem = scanner.nextLine();

                if (mensagem.startsWith("/upload")) {
                    String filePath = mensagem.split(" ", 2)[1]; // Extrai o caminho do arquivo
                    enviaArquivo(filePath);
                } else if (mensagem.startsWith("/download")) {
                    String fileName = mensagem.split(" ", 2)[1]; // Extrai o nome do arquivo
                    bufferedWriter.write("DOWNLOAD " + fileName);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    recebeArquivo(fileName); // Recebe o arquivo do servidor
                } else {
                    bufferedWriter.write(username + ": " + mensagem);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    public void enviaArquivo(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                bufferedWriter.write("UPLOAD " + file.getName());
                bufferedWriter.newLine();
                bufferedWriter.flush();

                // Enviar o arquivo
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    socket.getOutputStream().write(buffer, 0, bytesRead);
                }
                socket.getOutputStream().flush();
                fileInputStream.close();
                System.out.println("Arquivo enviado: " + filePath);

                // Notificar os outros clientes sobre o arquivo enviado
                bufferedWriter.write(username + " enviou o arquivo: " + file.getName());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                System.out.println("Arquivo não encontrado!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void recebeArquivo(String fileName) {
        try {
            File file = new File("download_" + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                if (bytesRead < 1024) break;  // Se for menor que o buffer, fim da transferência
            }

            fileOutputStream.close();
            System.out.println("Arquivo recebido: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recebeMensagem(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String mensagemChat;

                while(socket.isConnected()){
                    try{
                        mensagemChat = bufferedReader.readLine();
                        System.out.println(mensagemChat);
                    } catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedReader!=null) {
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main (String [] args) throws IOException {
        Scanner scanner = new Scanner(System.in); //
        System.out.println("Entre o nome do seu usuário para ser usado no Chat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        client.recebeMensagem();
        client.enviaMensagem();
    }
}
