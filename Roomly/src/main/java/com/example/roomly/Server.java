// configura um servidor que pode se comunicar com um cliente via sockets, lê mensagens do cliente e exibe no front-end usando VBox.

package com.example.roomly; // Define o pacote onde essa classe está localizada

import javafx.scene.layout.VBox; // Importa a classe VBox do JavaFX, que será usada para a interface gráfica

import java.io.*; // Importa classes para manipulação e saída de dados
import java.net.ServerSocket; // Importa a classe ServerSocket para criar o servidor
import java.net.Socket; // Importa a classe Socket para gerenciar a comunicação entre cliente e servidor

public class Server {

    private ServerSocket serverSocket; // declara uma variável para o socket do servidor
    private Socket socket; // declara uma variável para o socket que conecta o cliente
    private BufferedReader bufferedReader; // declara um leitor de buffer para receber dados
    private BufferedWriter bufferedWriter; // declara um escritor de buffer para enviar dados

    public Server(ServerSocket serverSocket) { // construtor da classe Server, que recebe um ServerSocket
        try{
            this.serverSocket = serverSocket; // inicializa o serverSocket com o parâmetro passado
            this.socket = serverSocket.accept(); // aguarda e aceita uma conexão de cliente
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // inicializa o bufferedReader para ler dados do cliente
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // inicializa o bufferedWriter para enviar dados do cliente
        } catch (IOException e) { // captura exceções de entrada e saída
            System.out.println("Erro ao criar o servidor.");
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessageToClient(String messageToClient) { //metodo para ENVIAR mensagens ao cliente
        try{
            bufferedWriter.write(messageToClient); // escreve a mensagem no buffer
            bufferedWriter.newLine(); // adiciona uma nova linha após a mensagem
            bufferedWriter.flush(); // envia a mensagem
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao enviar mensagem ao cliente.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void receiveMessageFromClient(VBox vBox) { // metodo para RECEBER mensagens do cliente
        new Thread(new Runnable() { // cria uma nova thread para receber mensagens de forma assincrona
            @Override
            public void run() { // implementando o metodo run da interface Runnable
                while(socket.isConnected()) { // enquanto o socket estiver conectado, continua a receber mensagens
                    try{
                        String messageFromClient = "Cliente: " + bufferedReader.readLine(); // lê uma linha do cliente e adiciona o prefixo "CLiente: "
                        Controller.addLabel(messageFromClient, vBox); // adiciona a mensagem na interface usando o metodo addLAbel do Controller
                    }catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Erro ao receber mensagem do cliente.");
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break; // encerra o loop ao capturar uma exceção
                    }
                }
            }
        }).start(); // inicia a thread para executar o metodo run
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) { // metodo para fechar recursos
        try{
            if(bufferedReader != null) { // verifica se o bufferedReader está aberto
                bufferedReader.close(); // se sim, o fecha
            }
            if(bufferedWriter != null) { // verifica se o bufferedWriter está aberto
                bufferedWriter.close(); // se sim, o fecha
            }
            if(socket != null) { // verifica se o socket está aberto
                socket.close(); // se sim, fecha o socket
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
