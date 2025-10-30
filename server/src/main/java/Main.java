import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import server.Server;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        try{
            var port = 8080;
            if (args.length >= 1){
                port = Integer.parseInt(args[0]);
            }

//            DataAccess dataAccess = new MemoryDataAccess();
            DataAccess dataAccess = new MySqlDataAccess();
            var server = new Server();
            server.run(port);
            System.out.println("♕ 240 Chess Server");
            return;
        } catch (Throwable ex){
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}