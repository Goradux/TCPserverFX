package tcpserverfx;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Albert Asratyan
 */

public class TCPserverFX extends Application {
    
    //userInput will contain whatever the user will enter
    //inputChanged will make sure that there is a new message, even  if it is a duplicate
    public static String userInput = "DEFAULTVALUE";
    public static boolean inputChanged = false;
    
    @Override
    public void start(Stage primaryStage) {
                
        AnchorPane userInterface = createUI();
        
        Scene scene = new Scene(userInterface, 300, 200);
        
        primaryStage.setTitle("PasteIT");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // close all of the background running code when the window is closed
        primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (userInput.equals("DEFAULTVALUE")) {
                            System.exit(0);
                        }
                        userInput = "quit";
                        inputChanged = true;
                    }
                });
            }
        });
    }

    public static void sendTextField(TextField text) {
        userInput = text.getText();
        inputChanged = true;
        text.clear();
    }
    
    public static void printTextField(TextArea textOutput, String text) {
        textOutput.setText(text);
    }
    
    public static void serverStart(TextArea textOutput)  throws Exception {
        String clientSentence;
        
        //This could be moved to prompt text line
        System.out.println("Your local IP address is " + InetAddress.getLocalHost().getHostAddress());
        
        //Creating a server socket
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket connectionSocket = serverSocket.accept();
        System.out.println("Client has connected. You can start chatting!");
        
        while (true) {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            if (inFromClient.ready() == true) {
                clientSentence = inFromClient.readLine();
                printTextField(textOutput, clientSentence);
                if (clientSentence.equals("quit")) {
                   connectionSocket.close();
                   serverSocket.close();
                   System.exit(0);
                }
            }
            
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            if (inputChanged == true) {
                outToClient.writeBytes(userInput + '\n');
                inputChanged = false;
                if (userInput.equals("quit")) {
                    connectionSocket.close();
                    serverSocket.close();
                    System.exit(0);
                }
            }
            Thread.sleep(50);
        }
    }
    
    //User interface code
    public AnchorPane createUI(){
        AnchorPane pane = new AnchorPane();
        pane.setMaxHeight(200);
        pane.setMaxWidth(300);
        pane.setMinHeight(200);
        pane.setMaxWidth(300);
        pane.setPrefHeight(200);
        pane.setPrefWidth(300);
        pane.setStyle("-fx-background-color: #cccccc;");
        
        // Top Pane
        Pane top = new Pane();
        top.setPrefHeight(60);
        top.setPrefWidth(300);
        top.setStyle("-fx-background-color: #52aa7f;");
        
        Label label = new Label();
        label.setLayoutX(29);
        label.setLayoutY(16);
        label.setStyle("-fx-border-color: #cccc; -fx-border-radius: 5;");
        label.setText("PasteIT");
        label.setTextFill(Paint.valueOf("#404040"));
        label.setFont(Font.font("System", FontWeight.BOLD, 18));
        top.getChildren().add(label);
        
        // Text Input
        TextField textIn = new TextField();
        textIn.setLayoutX(23);
        textIn.setLayoutY(67);
        textIn.setPrefHeight(25);
        textIn.setPrefWidth(215);
        textIn.setPromptText("Insert text");        
        
        textIn.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                sendTextField(textIn);
            }
        });
        
        // Text Output
        TextArea textOut = new TextArea();
        textOut.setLayoutX(22);
        textOut.setLayoutY(95);
        textOut.setPrefHeight(89);
        textOut.setPrefWidth(262);
        textOut.setPromptText("Nothing has been sent yet");       
        textOut.setWrapText(true);
        
        // Button
        Button btn = new Button();
        btn.setLayoutX(240);
        btn.setLayoutY(67);
        btn.setText("Send");
        btn.setMnemonicParsing(false);
        
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendTextField(textIn);
            }
        });
        
        pane.getChildren().addAll(top, textIn, textOut, btn);
        
        // Start the server in a separate thread
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    serverStart(textOut);
                } catch (Exception ex) {
                    Logger.getLogger(TCPserverFX.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };
        new Thread(task).start();
        
        return pane;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
