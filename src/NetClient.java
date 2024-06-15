import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class NetClient extends JFrame {

    final String serverIP = "127.0.0.1";
    final int serverPort = 1234;
    JTextArea textArea;
    JTextField textField;
    JScrollPane scrollPane;
    BufferedReader in;
    PrintWriter out;
    Thread theadIn;
    Socket socket;

    NetClient(){
        // Создаем окно
        super("Simple Chat client");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Добавляем на окно текстовое поле
        textArea = new JTextArea("",25,35);
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        textField = new JTextField(35);
        textField.setToolTipText("Ваш текст");
        textField.setFont(new Font("Dialog", Font.PLAIN, 14));
        textField.setHorizontalAlignment(JTextField.LEFT);
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Передаем введенный текст серверу
                out.print(textField.getText()+"\n");
                out.flush();
                textField.setText("");
            }
        });

        JPanel contents = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contents.add(new JScrollPane(textArea), BorderLayout.SOUTH );
        contents.add(textField, BorderLayout.NORTH);
        setContentPane(contents);

        // Подсоединяемся к серверу
        connect();

    }

    void connect() {
        try {
            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            textArea.setForeground(Color.RED);
            textArea.append("Server " + serverIP + " port " + serverPort + " " + "" + "NOT AVAILABLE");
            e.printStackTrace();
        }
        theadIn = new Thread() {
            // в отдельном потоке
            // принимаем символы от сервера
            public void run() {
                while (true) {
                    try {
                        addStringToTextArea(in.readLine());
                    } catch (IOException e) {
                        textArea.setForeground(Color.RED);
                        textArea.append("\nCONNECTION ERROR");
                        e.printStackTrace();
                        return;
                    }
                }
            };
        };

        theadIn.start();
    }

    public static void main(String[] args) {
        new NetClient().setVisible(true);
    }

    void addStringToTextArea(String str) {
        textArea.append(str+"\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
