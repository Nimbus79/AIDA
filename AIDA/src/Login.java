import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.KeyEvent;


public class Login implements ActionListener, KeyListener {
    private java.sql.Connection connection;
    private App app;
    private JFrame frame;
    private JTextField user;
    private JPasswordField password;
    private JButton login;
    private JLabel errorMsg;


    public Login() {
        this.connection = Connection.connect("aida", "oracle");
        if (this.connection != null) {
            System.out.println("AIDA: Connection Successful!");
        }
        this.app = new App();
        initComponents();
    }

    private void initComponents() {
        this.frame = this.app.createFrame(1);
        this.frame.add(this.app.createLabel(" NOME DE UTILIZADOR:", 160, 53, 180, 30, 15, false));
        createUserField();
        this.frame.add(this.app.createLabel("       PALAVRA-PASSE:", 160, 123, 180, 30, 15, false));
        createPasswordField();
        createLoginButton();
        createErrorMsgLabel();
    }

    private void createUserField() {
        this.user = new JTextField(15);
        this.user.setBounds(150, 83, 200, 30);
        this.user.addKeyListener(this);
        this.frame.add(this.user);
    }

    private void createPasswordField() {
        this.password = new JPasswordField(15);
        this.password.setBounds(150, 153, 200, 30);
        this.password.addKeyListener(this);
        this.frame.add(this.password);
    }

    private void createLoginButton() {
        this.login = this.app.createButton("Autenticar", 200,203,100,35, 14);
        this.login.addActionListener(this);
        this.frame.add(this.login);
    }

    private void createErrorMsgLabel() {
        this.errorMsg = this.app.createLabel("Credenciais incorretas", 177, 235, 146, 50, 14, true);
        this.errorMsg.setVisible(false);
        this.frame.add(this.errorMsg);
    }

    private boolean checkCredentials(String user, String password) throws SQLException {
        boolean result = false;
        PreparedStatement stm = this.connection.prepareStatement("SELECT COUNT(USERN)\n" +
                "FROM GESTAO.USERS\n" +
                "WHERE USERN = ? AND PALAVRA = ? AND APLICA LIKE '% BRO %'");
        stm.setString(1, user);
        stm.setString(2, password);
        ResultSet rs = stm.executeQuery();
        rs.next();
        int count = rs.getInt("COUNT(USERN)");
        rs.close();
        stm.close();
        if (count != 0)
            result = true;
        return result;
    }

    private void login() {
        String user = this.user.getText();
        String password = this.password.getText();
        try {
            if (checkCredentials(user, password)) {
                this.frame.setVisible(false);
                new MainWindow(this.connection, user);
            }
            else {
                this.errorMsg.setVisible(true);
                this.user.setText("");
                this.password.setText("");
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.login) {
            login();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER){
            this.login.doClick();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new Login();
        });
    }
}