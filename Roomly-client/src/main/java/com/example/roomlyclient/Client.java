package com.example.roomlyclient;

import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch(IOException e) {
            System.out.println("Erro ao criar o cliente.");
            e.printStackTrace();
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    // Envio de mensagens de texto
    public void sendMessageToServer(String messageToServer) {
        try {
            // Enviar cabeçalho indicando que é uma mensagem de texto
            bufferedWriter.write("TEXT");
            bufferedWriter.newLine();
            bufferedWriter.write(messageToServer);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao enviar mensagem ao servidor.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // Envio de arquivos
    public void sendFileToServer(File file) {
        try {
            // Enviar cabeçalho indicando que é um arquivo
            bufferedWriter.write("FILE");
            bufferedWriter.newLine();
            bufferedWriter.write(file.getName()); // Nome do arquivo
            bufferedWriter.newLine();
            bufferedWriter.write(String.valueOf(file.length())); // Tamanho do arquivo
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // Enviar o arquivo em si
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;

            OutputStream outputStream = socket.getOutputStream();
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao enviar arquivo ao servidor.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


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
