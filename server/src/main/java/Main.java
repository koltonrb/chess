import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import server.Server;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        try{
            var port = 8080;
            if (args.length >= 1){
                port = Integer.parseInt(args[0]);
            }

            DataAccess dataAccess = new MemoryDataAccess();
//            if (args.length >= 2 && args[1].equals("sql")){
//                dataAccess = new MySqlDataAccess();
//            }

            var userService = new UserService(dataAccess);
            var server = new Server(userService);
            server.run(port);
            System.out.println("♕ 240 Chess Server");
            return;
        } catch (Throwable ex){
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
    }
}