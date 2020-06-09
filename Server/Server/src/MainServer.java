import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MainServer extends javax.swing.JFrame {

    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Socket connection;
    private ServerSocket server;
    private int totalClients = 100;
    private int port = 6789;
    private PublicKey receiverpubKey;
    private KeyPair senderkeyPair;
    private PublicKey senderpubKey;
    private PrivateKey senderprivateKey;
    static Cipher ecipher, dcipher;//Required for DES

    public MainServer() {
        
        initComponents();
        this.setTitle("Server");
        this.setVisible(true);
        status.setVisible(true);
    }

    public static void main(String[] args) {

        MainServer myServer=new MainServer();
        myServer.startRunning();
    }

    public void startRunning()
    {

        try
        {
            server=new ServerSocket(port, totalClients);
            while(true)
            {
                try
                {
                    status.setText(" Waiting for Someone to Connect...");
                    connection=server.accept();
                    status.setText(" Now Connected to "+connection.getInetAddress().getHostName());

                    senderkeyPair = this.buildKeyPair();
                    senderpubKey = senderkeyPair.getPublic();
                    senderprivateKey = senderkeyPair.getPrivate();

                    output = new ObjectOutputStream(connection.getOutputStream());
                    output.writeObject(senderpubKey);
                    output.flush();
                    input = new ObjectInputStream(connection.getInputStream());
                    // send public key initially
                    Object pubKey = input.readObject();
                    receiverpubKey = (PublicKey) pubKey;
                    whileChatting();

                }catch(EOFException eofException)
                {
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }
        catch(IOException ioException)
        {
                ioException.printStackTrace();
        }
    }
    
   private void whileChatting() throws IOException
   {
        String message="";    
        jTextField1.setEditable(true);
        do{
                try
                {

                    message = (String) input.readObject();
                        chatArea.append("\n"+message);

                }catch(ClassNotFoundException classNotFoundException)
                {
                        
                }
        }while(!message.equals("Client - END"));
   }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        chatArea = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        status = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(null);

        chatArea.setColumns(20);
        chatArea.setRows(5);
        jScrollPane1.setViewportView(chatArea);

        jPanel1.add(jScrollPane1);
        jScrollPane1.setBounds(30, 110, 360, 270);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    jTextField1ActionPerformed(evt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        jPanel1.add(jTextField1);
        jTextField1.setBounds(30, 50, 270, 30);

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    jButton1ActionPerformed(evt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        jPanel1.add(jButton1);
        jButton1.setBounds(310, 50, 80, 30);

        status.setText("...");
        jPanel1.add(status);
        status.setBounds(30, 80, 300, 40);

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Write your text here");
        jPanel1.add(jLabel2);
        jLabel2.setBounds(30, 30, 150, 20);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bg7.jpg"))); // NOI18N
        jPanel1.add(jLabel1);
        jLabel1.setBounds(0, 0, 420, 405);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(417, 425));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) throws Exception {//GEN-FIRST:event_jButton1ActionPerformed
        
        sendMessage(jTextField1.getText(), senderpubKey, senderprivateKey, receiverpubKey);
	jTextField1.setText("");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) throws Exception {//GEN-FIRST:event_jTextField1ActionPerformed
        
        sendMessage(jTextField1.getText(),senderpubKey, senderprivateKey, receiverpubKey);
	jTextField1.setText("");
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void sendMessage(String message, PublicKey senderpubKey, PrivateKey senderprivateKey, PublicKey receiverpubKey)  throws Exception
    {
        try
        {
            chatArea.append("\nServer - "+message);

            System.out.print("\nPGP Simulation:\nSender Side: Input messsage= " + message);

            //Generating SHA-512 hash of original message
            String hashout = sha512(message);
            System.out.println("\nSender Side: Hash of Message=\n"+hashout);

            //Encrypt the message hash with sender private keys -> Digital Signature
            String encryptedprivhash = encrypt(senderpubKey, senderprivateKey, hashout, 0);
            System.out.println("\nSender Side: Hash Encrypted with Sender Private Key (Digital Signature)=\n"+ encryptedprivhash);

            //Append original message and encrypted hash
            String beforezipstring[] = {message, encryptedprivhash};
            System.out.println("\nSender Side: Message before Compression=\n"+beforezipstring[0]+beforezipstring[1]);

            //Apply zip to beforezipbytes[][]
            String afterzipstring[] = new String[beforezipstring.length];
            System.out.println("\nSender Side: Message after Compression=");
            for (int i=0;i<beforezipstring.length;i++) {
                afterzipstring[i] = compress(beforezipstring[i]);
                System.out.println(afterzipstring[i]);
            }

            //Encrypt zipstring with DES
            SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            System.out.println("\nSender Side: SecretKey DES=\n"+key.toString());
            String afterzipstringDES[] = new String[afterzipstring.length+1];
            System.out.println("\nSender Side: Compressed Message Encrypted with SecretKey=");
            for (int i=0;i<afterzipstring.length;i++) {
                afterzipstringDES[i] = encryptDES(afterzipstring[i], key);
                System.out.println(afterzipstringDES[i]);
            }

            //Encrypt DES key with Receiver Public Key using RSA
            String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
            //SecretKey is base64 encoded since direct string enccryption gives key in string format during decryption which cant be converted to SecretKey Format
            String keyencryptedwithreceiverpub = encrypt(receiverpubKey, null, encodedKey, 1);
            System.out.println("\nSender Side: DES SecretKey Encrypted with Receiver Public Key=\n"+keyencryptedwithreceiverpub);

            //Decrypting DES key with Receiver Private Key using RSA
            afterzipstringDES[2]=keyencryptedwithreceiverpub;
            String messagetoreceiver[] = afterzipstringDES;
            System.out.println("\nFinal Message to receiver=");
            for (int i=0;i<messagetoreceiver.length;i++) {
                System.out.println(messagetoreceiver[i]);
            }
            output.writeObject(messagetoreceiver);
            output.flush();


        }
        catch(IOException ioException)
        {
            chatArea.append("\n Unable to Send Message");
        }
    }



    public static String encryptDES(String str, SecretKey key) throws Exception {
        ecipher = Cipher.getInstance("DES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        // Encode the string into bytes using utf-8
        byte[] utf8 = str.getBytes("UTF8");
        // Encrypt
        byte[] enc = ecipher.doFinal(utf8);
        // Encode bytes to base64 to get a string
        return Base64.getEncoder().encodeToString(enc);
    }

    public static String compress(String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return Base64.getEncoder().encodeToString(compressed);
    }


    //Takes any string as input and calculates sha 512 bit hash. Output is in 128 bit hex string
    public static String sha512(String rawinput){
        String hashout = "";
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(rawinput.getBytes("utf8"));
            hashout = String.format("%040x", new BigInteger(1, digest.digest()));
        }
        catch(Exception E){
            System.out.println("Hash Exception");
        }
        return hashout;
    }


    //n: 0->encryptwithprivatekey 1->encryptwithpublickey
    public static String encrypt(PublicKey publicKey, PrivateKey privateKey, String message, int ch) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        if (ch == 0) {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(utf8);
        }
        else {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] utf8 = cipher.doFinal(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(utf8);
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
