//Hola
//Hola 2
//hola de adbel
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.xml.soap.Text;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat extends Application {
    private TextArea mensajes;

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane root = new BorderPane();
        mensajes = new TextArea();
        mensajes.setEditable(false);
        root.setCenter(mensajes);
        primaryStage.setTitle("Mensaje del servidor del chat");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

        HiloServidor hiloServidor = new HiloServidor();
        hiloServidor.start();
    }

    public void agregarMensajes(String mensaje) {
        Date hoy = new Date();
        String mensaje2 = String.format("[%2d/%2d/%4d %2d:%2d] %s\n",
                hoy.getDate(), hoy.getMonth() + 1, hoy.getYear() + 1900, hoy.getHours(),
                hoy.getMinutes(), mensajes);
        mensajes.appendText(mensaje2);
    }

    public static void main(String[] args) {
        launch(args);
    }

    class HiloServidor extends Thread {
        public void run(){
            try {
                List<HiloAtencion> listaHilos = new ArrayList<HiloAtencion>();

                ServerSocket servidor = new ServerSocket(4500, 5);
                while(true) {
                    Socket cliente = servidor.accept();
                    Platform.runLater(new Runnable(){
                        public void run(){
                            agregarMensajes("Se ha conectado un cliente: " + cliente);
                        }
                    });
                    HiloAtencion hilo = new HiloAtencion(cliente, listaHilos);
                    listaHilos.add(hilo);
                    hilo.start();
                }
            } catch (IOException e) {
            }
        }
    }

    class HiloAtencion extends Thread {
        private Socket cliente;
        private List<HiloAtencion> listaHilos;
        private BufferedReader lector;
        private BufferedWriter escritor;
        private String apodo="";

        public HiloAtencion(Socket cliente, List<HiloAtencion> listaHilos) {
            this.cliente = cliente;
            this.listaHilos = listaHilos;
        }

        public boolean enviarMensaje(String mensaje){
            try {
                escritor.write(mensaje+"\n");
                escritor.flush();
                return true;
            } catch (IOException e) {
                return false;
            }

        }

        public void run() {
            try {
                lector =
                        new BufferedReader(
                                new InputStreamReader(cliente.getInputStream()));
                escritor =
                        new BufferedWriter(
                                new OutputStreamWriter(cliente.getOutputStream()));
                System.out.println("Se ha conectado un cliente: " + cliente);

                enviarMensaje("+OK Bienvenido al servidor de mensajes");
                enviarMensaje("+REQ  Introduce el nombre de usuario y da ENTER: ");

                apodo = lector.readLine();

                boolean salir = false;
                while (!salir){
                    String cad= lector.readLine();
                    Platform.runLater(new Runnable(){
                        public void run(){
                            agregarMensajes("Usuario: "+apodo+", Cadena recibida: " + cad);
                        }
                    });
                    if (cad.equals("**SALIR**"))
                    {
                        salir = true;
                        break;
                    }
                    String mensaje= apodo + "> " + cad;
                    for(HiloAtencion conexion : listaHilos)
                        conexion.enviarMensaje(mensaje);
                }
                enviarMensaje("+OK Adios...");

                escritor.close();
                lector.close();
                cliente.close();
                listaHilos.remove(this);
            } catch (IOException e) {
            }
        }



    }


}




