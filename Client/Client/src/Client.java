
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.security.*;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

public class Client extends javax.swing.JFrame {

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String[] message;
    private String serverIP;
    private Socket connection;
    private PublicKey senderPublicKey;
    private int port = 6789;
    KeyPair receiverkeyPair;
    PublicKey receiverpubKey;
    PrivateKey receiverprivateKey;

    static Cipher dcipher;//Required for DES

    public Client(String s) {

        initComponents();

        this.setTitle("Client");
        this.setVisible(true);
        status.setVisible(true);
        serverIP = s;
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client("127.0.0.1");
        client.startRunning();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        chatArea = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        status = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setLayout(null);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jPanel1.add(jTextField1);
        jTextField1.setBounds(30, 50, 270, 30);

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);
        jButton1.setBounds(310, 50, 80, 30);

        chatArea.setColumns(20);
        chatArea.setRows(5);
        jScrollPane1.setViewportView(chatArea);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(30, 110, 360, 270);

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Write your text here");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(30, 30, 150, 20);

        status.setText("...");
        jPanel1.add(status);
        status.setBounds(30, 80, 300, 40);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bg7.jpg"))); // NOI18N
        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 0, 420, 410);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(414, 428));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

        sendMessage(jTextField1.getText());
        jTextField1.setText("");
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        sendMessage(jTextField1.getText());
        jTextField1.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed


    public void startRunning() throws Exception {
        try {
            status.setText("Attempting Connection ...");
            try {
                connection = new Socket(InetAddress.getByName(serverIP), port);
                receiverkeyPair = this.buildKeyPair();
                receiverpubKey = receiverkeyPair.getPublic();
                receiverprivateKey = receiverkeyPair.getPrivate();

            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(null, "Server Might Be Down!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
            status.setText("Connected to: " + connection.getInetAddress().getHostName());


            output = new ObjectOutputStream(connection.getOutputStream());
            output.writeObject(receiverpubKey);
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            Object pubKey = input.readObject();
            senderPublicKey = (PublicKey) pubKey;
            System.out.println(pubKey);
            whileChatting(senderPublicKey, null, receiverpubKey, receiverprivateKey);
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    private void whileChatting(PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey, PrivateKey receiverprivateKey) throws Exception {
        jTextField1.setEditable(true);
        do {
            try {
                message = (String[]) input.readObject();
                //Receiver receives the message messagetoreceiver[] with messagetoreceiver[2] as secret key encrypted with receiver pub key
                //Receiver decrypts the messagetoreceiver[2] with his/her privatekey
                String receiverencodedsecretkey = decrypt(receiverpubKey, receiverprivateKey, message[2], 1);
                //Key after decryption is in base64 encoded form
                byte[] decodedKey = Base64.getDecoder().decode(receiverencodedsecretkey);
                SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "DES");
                System.out.println("\nReceiver Side: Receiver SecretKey DES after Decryption with his/her Private Key=\n" + originalKey.toString());

                //Decrypt the rest of the message in messagetoreceiver with SecretKey originalKey
                String receiverdecryptedmessage[] = new String[message.length - 1];
                System.out.println("\nReceiver Side: Message After Decryption with SecretKey=");
                for (int i = 0; i < message.length - 1; i++) {
                    message[i] = decryptDES(message[i], originalKey);
                    System.out.println(message[i]);
                }

                String unzipstring[] = new String[receiverdecryptedmessage.length];
                System.out.println("\nReceiver Side: UnZipped Message=");
                for (int i = 0; i < unzipstring.length; i++) {
                    unzipstring[i] = decompress(message[i]);
                    System.out.println(unzipstring[i]);
                }
                //Unzip this message now i.e. unzip messa

                //Message has been received and is in unzipstring but check the digital signature of the sender i.e. verify the hash with senderpubkey
                //So decrypting the encrypted hash in unzipstring with sender pub key
                String receivedhash = decrypt(senderpubKey, senderprivateKey, unzipstring[1], 0);
                System.out.println("\nReceiver Side: Received Hash=\n" + receivedhash);
                //Calculating SHA512 at receiver side of message
                String calculatedhash = sha512(unzipstring[0]);
                System.out.println("\nReceiver Side: Calculated Hash by Receiver=\n" + calculatedhash);
                if (receivedhash.equalsIgnoreCase(calculatedhash)) {
                    System.out.println("\nReceived Hash = Calculated Hash\nThus, Confidentiality and Authentication both are achieved\nSuccessful PGP Simulation\n");
                }

                chatArea.append("\nServer - " + unzipstring[0]);

            } catch (ClassNotFoundException classNotFoundException) {
            }
        } while (!message.equals("Client - END"));
    }


    private void sendMessage(String message) {
        try {
            output.writeObject("Client - " + message);
            output.flush();
            chatArea.append("\nClient - " + message);
        } catch (IOException ioException) {
            chatArea.append("\n Unable to Send Message");
        }
    }

    public static String decryptDES(String st, SecretKey key) throws Exception {
        dcipher = Cipher.getInstance("DES");
        dcipher.init(Cipher.DECRYPT_MODE, key);
        // Decode base64 to get bytes
        byte[] dec = Base64.getDecoder().decode(st);
        byte[] utf8 = dcipher.doFinal(dec);
        // Decode using utf-8
        return new String(utf8, "UTF8");
    }


    public static String decompress(String st) throws IOException {
        byte[] compressed = Base64.getDecoder().decode(st);
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        gis.close();
        bis.close();
        return sb.toString();
    }

    //Takes any string as input and calculates sha 512 bit hash. Output is in 128 bit hex string
    public static String sha512(String rawinput) {
        String hashout = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(rawinput.getBytes("utf8"));
            hashout = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception E) {
            System.out.println("Hash Exception");
        }
        return hashout;
    }


    //n: 0->decryptwithpublickey 1->decryptwithprivatekey
    public static String decrypt(PublicKey publicKey, PrivateKey privateKey, String st, int ch) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        byte[] encrypted = Base64.getDecoder().decode(st);
        if (ch == 0) {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] utf8 = cipher.doFinal(encrypted);
            return new String(utf8, "UTF8");
        } else {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] utf8 = cipher.doFinal(encrypted);
            return new String(utf8, "UTF8");
        }
    }

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea chatArea;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel status;
    // End of variables declaration//GEN-END:variables
}
