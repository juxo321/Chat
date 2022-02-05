//Hola
//Hola 2
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Chat extends Application {

    public static void main(String[] args) {
        try {
            List<HiloAtencion> listaHilos = new ArrayList<HiloAtencion>();

            ServerSocket servidor = new ServerSocket(4500, 5);
            while(true) {
                Socket cliente = servidor.accept();
                HiloAtencion hilo = new HiloAtencion(cliente, listaHilos);
                listaHilos.add(hilo);
                hilo.start();
            }
        } catch (IOException e) {
        }
        //launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("App");
        //Login pantallaLogin = new Login();
        //Scene scene = new Scene(pantallaLogin, 1200, 800);
        //primaryStage.setScene(scene);
        primaryStage.show();
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
            enviarMensaje("Introduce eu nombre de usuario y dar ENTER: ");

            apodo = lector.readLine();

            boolean salir = false;
            while (!salir){
                String cad= lector.readLine();
                System.out.println("Cadena recibida: " + cad);
                if(cad.equals("**SALIR**")){
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
