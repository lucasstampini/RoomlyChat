import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList <ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String usernameCliente;

    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.usernameCliente = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVIDOR: " + usernameCliente + " entrou no Chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run(){
        String mensagemDoCliente;
        while (socket.isConnected()){
            try{
                mensagemDoCliente = bufferedReader.readLine();
                if (mensagemDoCliente == null) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }

                if (mensagemDoCliente.startsWith("UPLOAD")) {
                    String fileName = mensagemDoCliente.split(" ", 2)[1];
                    recebeArquivo(fileName);
                    broadcastMessage(usernameCliente + " enviou o arquivo: " + fileName);
                } else if (mensagemDoCliente.startsWith("DOWNLOAD")) {
                    String fileName = mensagemDoCliente.split(" ", 2)[1];
                    enviaArquivo(fileName);
                } else {
                    broadcastMessage(mensagemDoCliente);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void recebeArquivo(String fileName) {
        try {
            File file = new File("server_" + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = socket.getInputStream().read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
                if (bytesRead < 1024) break; // fim da transferência
            }

            fileOutputStream.close();
            System.out.println("Arquivo recebido: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviaArquivo(String fileName) {
        try {
            File file = new File("server_" + fileName);
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    socket.getOutputStream().write(buffer, 0, bytesRead);
                }
                socket.getOutputStream().flush();
                fileInputStream.close();
                System.out.println("Arquivo enviado: " + fileName);
            } else {
                bufferedWriter.write("SERVIDOR: Arquivo não encontrado.");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String mensagem){
        for (ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.usernameCliente.equals(usernameCliente)){
                    clientHandler.bufferedWriter.write(mensagem);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVIDOR: " + usernameCliente + " saiu do Chat!");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null) {
                bufferedWriter.close();
            }
            if(socket!=null) {
                socket.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
