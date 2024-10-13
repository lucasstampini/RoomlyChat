package com.example.roomly;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;

public class Server {
    @FXML
    private VBox vbox_messages;
    @FXML
    private ScrollPane sp_main;
    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public Server(ServerSocket serverSocket) {
        try{
            this.serverSocket = serverSocket;
            this.socket = serverSocket.accept();
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Erro ao criar o servidor.");
            e.printStackTrace();
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessageToClient(String messageToClient) {
        try{
            bufferedWriter.write(messageToClient);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao enviar mensagem ao cliente.");
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void receiveMessageFromClient(VBox vBox) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Definir um timeout no socket para evitar bloqueios indefinidos
                    socket.setSoTimeout(300000); // 30 segundos de timeout

                    while (socket.isConnected()) {
                        try {
                            // Leia o cabeçalho para identificar o tipo de dado
                            String header = bufferedReader.readLine(); // O cabeçalho vem primeiro

                            if (header == null) {
                                throw new IOException("Conexão fechada pelo cliente");
                            }

                            if ("TEXT".equals(header)) {
                                // Receber uma mensagem de texto
                                String messageFromClient = "Cliente: " + bufferedReader.readLine();
                                Platform.runLater(() -> Controller.addLabel(messageFromClient, vBox));
                            } else if ("FILE".equals(header)) {
                                // Receber um arquivo
                                String fileName = bufferedReader.readLine();  // Nome do arquivo
                                int fileSize = Integer.parseInt(bufferedReader.readLine());  // Tamanho do arquivo

                                // Ler o arquivo do stream em memória até o tamanho esperado
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                int totalBytesRead = 0;
                                InputStream inputStream = socket.getInputStream();

                                // Ler o arquivo até atingir o tamanho esperado
                                while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer, 0, Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                                    baos.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;
                                }

                                // Verifica
                                if (totalBytesRead < fileSize) {
                                    throw new IOException("Arquivo incompleto recebido");
                                }

                                // Obter os dados do arquivo em um array de bytes
                                byte[] fileData = baos.toByteArray();
                                baos.close();

                                // Obter o diretório atual do projeto
                                String projectDir = System.getProperty("user.dir");

                                // Criar o caminho do arquivo no diretório do projeto
                                File receivedFile = new File(projectDir, fileName);

                                // Salvar o arquivo recebido no diretório do projeto
                                try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
                                    fos.write(fileData);
                                }

                                // Criar um Hyperlink que permite o download do arquivo
                                Platform.runLater(() -> {
                                    Hyperlink fileLink = new Hyperlink("Baixar arquivo: " + fileName);
                                    fileLink.setOnAction(e -> {
                                        downloadFile(receivedFile);
                                    });
                                    vBox.getChildren().add(fileLink); // Adiciona o Hyperlink à interface
                                });
                            }
                        } catch (SocketTimeoutException e) {
                            Platform.runLater(() -> Controller.addLabel("Conexão com o cliente expirou.", vBox));
                            closeEverything(socket, bufferedReader, bufferedWriter);
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> Controller.addLabel("Erro ao receber dados do cliente.", vBox));
                            closeEverything(socket, bufferedReader, bufferedWriter);
                            break;
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> Controller.addLabel("Erro ao configurar o timeout do socket.", vBox));
                }
            }
        }).start();
    }

    // Função para "baixar" o arquivo ao clicar no link
    private void downloadFile(File file) {
        // Escolher onde salvar o arquivo baixado
        FileChooser fileChooser = new FileChooser();
        String ext1 = FilenameUtils.getExtension(String.valueOf(file));
        fileChooser.setInitialFileName(file.getName() + ext1);

        // Abrir um diálogo para o usuário escolher onde salvar o arquivo
        File destination = fileChooser.showSaveDialog(null);

        if (destination != null) {
            try {
                // Copiar o arquivo para o destino escolhido
                Files.copy(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Arquivo baixado com sucesso: " + destination.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erro ao baixar o arquivo.");
            }
        }
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
