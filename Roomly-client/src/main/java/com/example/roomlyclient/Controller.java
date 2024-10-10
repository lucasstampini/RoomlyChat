package com.example.roomlyclient;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    private Button button_send;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ScrollPane sp_main;
    @FXML
    private Label typingStatusLabel;  // Label para exibir a mensagem de "está digitando..."

    private Client client; // Instância da classe Client para interagir com o servidor

    // inicializa a interface do usuário e estabelece uma conexão com um servidor
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            client = new Client(new Socket("localhost", 1234)); // cria um novo cliente e conecta ao servidor na porta 1234
            System.out.println("Conectado ao servidor."); // mensagem de sucesso
        } catch (IOException e) {
            e.printStackTrace();
        }

        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });

        client.receiveMessageFromServer(vbox_messages); // inicia a escuta de mensagens do servidor

        button_send.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String messageToSend = tf_message.getText(); // recupera a mensagem digitada pelo usuário
                if (!messageToSend.isEmpty()) { // verifica se a mensagem não esta vazia
                    HBox hBox = new HBox(); // cria um objeto HBox para organização das mensagens
                    hBox.setAlignment(Pos.CENTER_RIGHT); // alinha a mensagem a direita
                    hBox.setPadding(new Insets(5, 5, 5, 5)); // define o espaçamento entre as mensagens
                    Text text = new Text(messageToSend); // cria um objeto Text para a mensagem
                    TextFlow textFlow = new TextFlow(text); // cria um objeto TextFlow para estilizar a mensagem
                    textFlow.setStyle("-fx-color: rgb(239,242,255); " +
                            "-fx-background-color: rgb(15,125,242);" +
                            "-fx-background-radius: 20px");

                    textFlow.setPadding(new Insets(5, 10, 5, 10));
                    text.setFill(Color.color(0.934, 0.945, 0.996));

                    hBox.getChildren().add(textFlow); // adiciona a mensagem ao HBox
                    vbox_messages.getChildren().add(hBox); // adiciona o HBox ao VBox

                    client.sendMessageToServer(messageToSend); // envia a mensagem para o servidor
                    tf_message.clear(); // limpa o campo de entrada de mensagens após o envio
                    client.sendTypingStatus(false);  // Envia sinal de que parou de digitar
                }
            }
        });

        // Detecta quando o cliente começa a digitar
        tf_message.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                client.sendTypingStatus(true);  // Envia sinal de que está digitando
            } else {
                client.sendTypingStatus(false);  // Envia sinal de que parou de digitar
            }
        });

        // Botão para enviar uma imagem
        Button button_sendImage = new Button("Enviar Imagem");
        button_sendImage.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                client.sendImageToServer(selectedFile);  // Envia a imagem ao servidor
            }
        });
        vbox_messages.getChildren().add(button_sendImage);  // Adiciona o botão ao VBox da interface

    }

    // Este método Java, `addLabel`, adiciona uma mensagem de texto estilizada a uma caixa vertical (`VBox`) em um aplicativo JavaFX. A mensagem é recebida de um cliente e exibida com um fundo cinza claro e cantos arredondados. A adição da mensagem à `VBox` é feita na thread do aplicativo JavaFX usando `Platform.runLater`.
    public static void addLabel(String msgFromServer, VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(msgFromServer);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
                "-fx-background-radius: 20px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        hBox.getChildren().add(textFlow);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBox.getChildren().add(hBox);
            }
        });
    }
}