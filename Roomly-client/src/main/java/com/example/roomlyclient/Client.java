package com.example.roomlyclient;

import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;

public class Client { // declarando a classe Client, que será responsável pela comunicação de rede com o servidor

    private Socket socket; // objeto Socket para representar a comunicação com o servidor
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    // Inicializa a conexão do cliente com o servidor. Cria BufferedReader e BufferedWriter para leitura e escrita no socket.
    public Client(Socket socket) { // construtor da classe Client, recebe um Socket como argumento
        try {
            this.socket = socket; // inicializa o campo Socket com o parâmetro conhecido
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch(IOException e) {
            System.out.println("Erro ao criar o cliente.");
            e.printStackTrace();
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    // Escreve uma mensagem e a envia para o servidor através do bufferedWriter.
    public void sendMessageToServer(String messageToServer) {
        try{
            bufferedWriter.write(messageToServer);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao enviar mensagem ao servidor.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Recebe mensagens do servidor em uma nova thread, o que permite que a aplicação continue respondendo a outras ações
    // enquanto recebe dados. As mensagens recebidas são passadas para a interface gráfica por meio de um VBox.
    public void receiveMessageFromServer(VBox vBox) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(socket.isConnected()) {
                    try{
                        String messageFromClient = bufferedReader.readLine();
                        Controller.addLabel(messageFromClient, vBox);
                    }catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Erro ao receber mensagem do cliente.");
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        }).start();
    }

    // Fecha todos os recursos de rede e de E/S para liberar a memória e evitar vazamento de recursos.
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try{
            if(bufferedReader != null) {
                bufferedReader.close();
            }
            if(bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
