import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {
    private Map<Integer, Client> clients = new HashMap<>();
    private ServerSocket serverSocket;
    private int nextClientKey;
    public ChatServer() {
        this.nextClientKey = 0;
        // создаем серверный сокет на порту 1234
        try {
            serverSocket = new ServerSocket(1234);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            System.out.println("Waiting...");
            // ждем клиента из сети
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected!");
                // создаем клиента на своей стороне
                nextClientKey++;
                clients.put(nextClientKey, new Client(socket, nextClientKey));
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void sendAll(String message, int senderInd){
        Iterator<Integer> iterator = clients.keySet().iterator();
        while (iterator.hasNext()) {
            int key = iterator.next();
            Client client = clients.get(key);
            if( client.isAutorized){
                if(client.index != senderInd)
                    client.receive(message);
                else
                    client.receive(String.format("\t%s",message));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new ChatServer().run();
    }

    class Client implements Runnable {
        private Socket socket;
        private String name;
        private int index;
        boolean isAutorized;
        public Calendar calendar;
        public SimpleDateFormat dateFormat;
        public SimpleDateFormat timeFormat;
        Scanner in;
        PrintStream out;

        InputStream is;
        OutputStream os;

        Thread thread;

        public Client(Socket socket, int index){
            this.socket = socket;
            this.index = index;
            this.isAutorized = false;

            this.calendar = Calendar.getInstance();
            this.timeFormat = new SimpleDateFormat("HH:mm:ss");
            this.dateFormat = new SimpleDateFormat("dd:MMM");

            // запускаем поток
            try {
                this.thread = new Thread(this);
                this.thread.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                // получаем потоки ввода и вывода
                is = socket.getInputStream();
                os = socket.getOutputStream();

                // создаем удобные средства ввода и вывода
                in = new Scanner(is);
                out = new PrintStream(os);

                // читаем из сети и пишем в сеть
                calendar = Calendar.getInstance();
                out.println(dateFormat.format(calendar.getTime()));

                out.println("Wellcome dear visitor! Chat is available after login. \nWhat is your name?");
                String input = in.nextLine();
                name = input;
                this.isAutorized = true;
                sendAll(String.format("%s enter to chat!", name), index);

                while (true) {
                    input = in.nextLine();
                    if(input.equals("exit")) {
                        closeAll();
                        break;
                    }
                    else
                        calendar = Calendar.getInstance();
                    sendAll(String.format("[%s] %s: %s", timeFormat.format(calendar.getTime()), name, input), index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (NoSuchElementException e) {
                closeAll();
            }
            finally {
                System.out.println(String.format("Now in chat %d visitors", clients.size()));
            }
        }

        public void closeAll(){
            try {
                is.close();
                os.close();
                in.close();
                out.close();
                socket.close();
                thread.interrupt();
                clients.remove(index);
                sendAll(String.format("%s exit from chat", name), index);
                System.out.println("Client closed");
            }catch (IOException e){
                System.out.println("Не удается остановить службы клиента");
            }
        }

        void receive(String message){
            out.println(message);
        }
    }
}