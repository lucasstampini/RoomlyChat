package com.example.roomly;

// Bibliotecas para estilizar a interface
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Bibliotecas para comunicação
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable { // Declara a classe Controller que implementa Initializable.

    public AnchorPane ap_main; // Define o painel principal da interface
    @FXML
    private Button button_send; // Define o botão de envio para mensagens
    @FXML
    private TextField tf_message; // Define o campo de texto para envio de mensagens
    @FXML
    private VBox vbox_messages; // Caixa vertical para organizar as mensagens
    @FXML
    private ScrollPane sp_main; // Painel de rolagem para a caixa de mensagens

    private Server server; // Instância do servidor para comunicação com o cliente

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {// Método chamado ao carregar a interface

        try {
            server = new Server(new ServerSocket(1234)); // Cria o servidor e o inicia na porta 1234
        } catch (IOException e) { 
            e.printStackTrace();
            System.out.println("Erro ao criar o servidor.");
        }

        vbox_messages.heightProperty().addListener(new ChangeListener<Number>() { // Adiciona um listener para a altura da vbox
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue); // Rola para o final da ScrollPane quando uma nova mensagem é adicionada
            }
        });

        server.receiveMessageFromClient(vbox_messages);  // Inicia a escuta de mensagens do cliente

        button_send.setOnAction(new EventHandler<ActionEvent>() { // Define uma ação para o botão de mensagens
            @Override
            public void handle(ActionEvent event) {
                String messageToSend = tf_message.getText(); // Recupera a mensagem digitada pelo cliente
                if(!messageToSend.isEmpty()){ // Verifica se a mensagem não esta vazia
                    HBox hBox = new HBox(); // Cria um objeto HBox para organização das mensagens
                    hBox.setAlignment(Pos.CENTER_RIGHT); // Alinha a mensagem a direita
                    hBox.setPadding(new Insets(5, 5, 5, 5)); // Define o espaçamento entre as mensagens

                    Text text = new Text(messageToSend); // Cria um objeto Text para a mensagem
                    TextFlow textFlow = new TextFlow(text); // Cria um objeto TextFlow para estilizar a mensagem

                    textFlow.setStyle("-fx-color: rgb(239,242,255); " +
                            "-fx-background-color: rgb(15,125,242);" +
                            "-fx-background-radius: 20px;");

                    textFlow.setPadding(new Insets(5, 10, 5, 10));
                    text.setFill(Color.color(0.934,0.945,0.996));

                    hBox.getChildren().add(textFlow);
                    vbox_messages.getChildren().add(hBox);

                    server.sendMessageToClient(messageToSend); // Envia a mensagem para o cliente
                    tf_message.clear(); // Limpa o campo de entrada de mensagens após o envio
                }
            }
        });

        // Método auxiliar para adicionar uma imagem ao VBox
        public void addImageToVBox(File imageFile) {
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(200);  // Ajusta o tamanho da imagem, se necessário
            imageView.setPreserveRatio(true);

            HBox hBox = new HBox(imageView);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 5, 5, 10));

            Platform.runLater(() -> vbox_messages.getChildren().add(hBox));
        }
    }
    // Este método Java, `addLabel`, adiciona uma mensagem de texto estilizada a uma caixa vertical (`VBox`) em um aplicativo JavaFX. A mensagem é recebida de um cliente e exibida com um fundo cinza claro e cantos arredondados. A adição da mensagem à `VBox` é feita na thread do aplicativo JavaFX usando `Platform.runLater`.
    public static void addLabel(String messageFromClient, VBox vBox){
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromClient);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle( "-fx-background-color: rgb(233,233,235);" +
                "-fx-background-radius: 20px;");
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