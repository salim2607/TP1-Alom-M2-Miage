package fr.lille.alom;

/**
 * Serveur Web Java
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Démarrage du serveur web..." );
        
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide: " + args[0] + ". Utilisation du port 8080.");
            }
        }

        WebServer server = new WebServer(port);
        
        // Gestion propre de l'arrêt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Arrêt du serveur...");
            server.stop();
        }));

        server.start();
    }
}