package GUI;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import core.MessageClass;
import core.TopicClass;
import user.User;

import javax.swing.*;
import java.awt.*;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 *
 * @author phibonachos
 */
public class DashFrame extends javax.swing.JFrame {
    private boolean Triggered;
    private User myClient;
    String ServerHost;
    JFrame Caller;

    private class PollingThread extends Thread{
        private User toListen;
        public PollingThread(User u){
            toListen = u;
        }

        @Override
        public void run(){
            HashMap<String, TopicClass> status = toListen.getServerTopics();
            while(status==toListen.getServerTopics());
            status = toListen.getServerTopics();
            System.err.println("Updating....");
            updateTopic();
        }

    }


    /**************************************************************************/
    public void updateConvo(){
        ConvoBox.removeAll();
        // add as many msgBox as the message in topic...
        if(!myClient.getTopics().contains(TopicConvoName)) return;
        for(String msg : myClient.getConvo(TopicConvoName.getText())){
            ConvoBox.add(new MsgBox(msg));
        }
        ConvoBox.revalidate();
        ConvoBox.repaint();
        pack();
        return;
    }

    public void updateTopic(){
        SubBox.removeAll();
        for(String top : myClient.getTopics()){
            SubBox.add(new SubForm(top, myClient.getMyTopics().get(top)));
        }
        ConvoBox.revalidate();
        ConvoBox.repaint();
        pack();
        return;
    }
    /**************************************************************************/


    /*-----------------------------------------------------------------------*/
    private class MsgBox extends javax.swing.JTextArea{
        public MsgBox(String Message){
            setColumns(20);
            setRows(5);
            setLineWrap(true);
            setWrapStyleWord(true);
            setMinimumSize(new java.awt.Dimension(290, 38));
            setEditable(false);
            setBackground(new java.awt.Color(103, 106, 108));
            setForeground(new java.awt.Color(255, 255, 255));
            setFont(new java.awt.Font("Dialog", 0, 10));
            setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(70,73,76), 2), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(103, 106, 108), 5)));
            setText(Message);
            setMaximumSize(new Dimension(getPreferredSize()));
        }
    }

    private class SubForm extends javax.swing.JPanel {
        private String TopicName;
        private boolean Triggered;
        private JToggleButton SubButton;
        private JButton CheckoutButton;

        public SubForm(String tn, boolean pushed) {
            TopicName = tn;
            if(myClient.getNotifier().contains(tn) && !TopicConvoName.equals(TopicName))setBackground(new java.awt.Color(206,109,139));
            else setBackground(new java.awt.Color(139, 137, 130));
            setPreferredSize(new Dimension(163, 120));
            setMaximumSize(new java.awt.Dimension(getPreferredSize()));
            setMinimumSize(new java.awt.Dimension(getPreferredSize()));
            setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(35, 37, 43), 2));
            SubButton = new JToggleButton();
            CheckoutButton = new JButton();

            JLabel TopicName = new JLabel();

            SubButton.setBackground(new java.awt.Color(76, 92, 104));
            SubButton.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
            SubButton.setForeground(new java.awt.Color(133, 189, 191));
            SubButton.setText("Subscribe");
            SubButton.setBorder(null);
            SubButton.setPreferredSize(new Dimension(93, 25));
            SubButton.setSelected(pushed);
            SubButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    SubButtonActionPerformed(evt);
                }
            });

            CheckoutButton.setBackground(new java.awt.Color(76, 92, 104));
            CheckoutButton.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
            CheckoutButton.setForeground(new java.awt.Color(133, 189, 191));
            CheckoutButton.setText("Checkout");
            CheckoutButton.setBorder(null);
            CheckoutButton.setPreferredSize(new Dimension(93, 25));
            CheckoutButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    CheckoutButtonActionPerformed(evt);
                }
            });

            TopicName.setText(this.TopicName);

            javax.swing.GroupLayout TopicSubFormLayout = new javax.swing.GroupLayout(this);
            setLayout(TopicSubFormLayout);
            TopicSubFormLayout.setHorizontalGroup(
                    TopicSubFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(TopicSubFormLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(TopicSubFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(TopicName, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                                            .addComponent(SubButton, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                                            .addComponent(CheckoutButton, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                                    .addContainerGap())
            );
            TopicSubFormLayout.setVerticalGroup(
                    TopicSubFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, TopicSubFormLayout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addComponent(TopicName)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(CheckoutButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(SubButton)
                                    .addContainerGap())
            );

        }

        private void SubButtonActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
            if (!Triggered) {
                // call from server class
                try {
                    myClient.SubscribeRequest(TopicName, "subscribe");
                } catch (RemoteException e) {
                    System.err.println("Cannot subscribe honey...");
                }
                SubButton.setText("Unsubscribe");
            } else {
                try {
                    myClient.SubscribeRequest(TopicName, "unsubscribe");
                } catch (RemoteException e) {
                    System.err.println("Cannot unsubscribe honey...");
                }
                SubButton.setText("Subscribe");
            }
            Triggered = !Triggered;
        }

        private void CheckoutButtonActionPerformed(java.awt.event.ActionEvent evt) {
            // TODO add your handling code here:
            // flush convoBox
            TopicConvoName.setText(TopicName);
            SendButton.setEnabled(Triggered);
            setBackground(new java.awt.Color(139, 137, 130));
            updateConvo();
            myClient.removeNotif(TopicName);
        }
    }
    /*-----------------------------------------------------------------------*/
    /**
     * Creates new form DashFrame
     */
    public DashFrame(User user, String sh, JFrame c) {
        initComponents();
        ServerHost = sh;
        myClient = user;
        Caller = c;
        updateTopic();
        SendButton.setEnabled(false);
        PollingThread pt = new PollingThread(myClient);
        pt.start();
        // populate convoBox
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        MainPane = new javax.swing.JPanel();
        UtilityPane = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        DisconnectButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        SubBox = new javax.swing.JPanel();
        AddTopic = new javax.swing.JButton();
        TopicNameField = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        ConvoPane = new javax.swing.JPanel();
        MessageField = new javax.swing.JTextField();
        MessageSep = new javax.swing.JSeparator();
        SendButton = new javax.swing.JButton();
        ConvoScroll = new javax.swing.JScrollPane();
        ConvoBox = new javax.swing.JPanel();
        TopicConvoName = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        MainPane.setBackground(new java.awt.Color(70, 73, 76));
        MainPane.setToolTipText("");
        MainPane.setMaximumSize(new java.awt.Dimension(664, 501));

        UtilityPane.setBackground(new java.awt.Color(47, 50, 58));

        jSeparator1.setBackground(new java.awt.Color(70, 73, 76));
        jSeparator1.setForeground(new java.awt.Color(70, 73, 76));

        DisconnectButton.setBackground(new java.awt.Color(76, 92, 104));
        DisconnectButton.setForeground(new java.awt.Color(133, 189, 191));
        DisconnectButton.setText("Disconnect");
        DisconnectButton.setBorder(null);
        DisconnectButton.setName(""); // NOI18N
        DisconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(139, 200));

        SubBox.setBackground(new java.awt.Color(35, 37, 43));
        SubBox.setLayout(new javax.swing.BoxLayout(SubBox, javax.swing.BoxLayout.PAGE_AXIS));
        jScrollPane1.setViewportView(SubBox);

        AddTopic.setBackground(new java.awt.Color(76, 92, 104));
        AddTopic.setForeground(new java.awt.Color(133, 189, 191));
        AddTopic.setText("Add Topic");
        AddTopic.setBorder(null);
        AddTopic.setName(""); // NOI18N
        AddTopic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddTopicActionPerformed(evt);
            }
        });

        TopicNameField.setBackground(new java.awt.Color(47, 50, 58));
        TopicNameField.setForeground(new java.awt.Color(139, 137, 130));
        TopicNameField.setText("...");
        TopicNameField.setBorder(null);
        TopicNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TopicNameFieldActionPerformed(evt);
            }
        });
        TopicNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TopicNameFieldKeyPressed(evt);
            }
        });

        jSeparator2.setBackground(new java.awt.Color(70, 73, 76));
        jSeparator2.setForeground(new java.awt.Color(70, 73, 76));

        javax.swing.GroupLayout UtilityPaneLayout = new javax.swing.GroupLayout(UtilityPane);
        UtilityPane.setLayout(UtilityPaneLayout);
        UtilityPaneLayout.setHorizontalGroup(
                UtilityPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(UtilityPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(UtilityPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jSeparator1)
                                        .addComponent(DisconnectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(AddTopic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                                        .addComponent(TopicNameField, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jSeparator2))
                                .addContainerGap())
        );
        UtilityPaneLayout.setVerticalGroup(
                UtilityPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, UtilityPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(TopicNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AddTopic, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(DisconnectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        ConvoPane.setBackground(new java.awt.Color(70, 73, 76));

        MessageField.setBackground(new java.awt.Color(70, 73, 76));
        MessageField.setFont(new java.awt.Font("URW Gothic", 0, 10)); // NOI18N
        MessageField.setForeground(new java.awt.Color(197, 195, 198));
        MessageField.setToolTipText("");
        MessageField.setBorder(null);
        MessageField.setMaximumSize(new java.awt.Dimension(377, 13));
        MessageField.setMinimumSize(new java.awt.Dimension(377, 13));

        MessageSep.setBackground(new java.awt.Color(47, 50, 58));
        MessageSep.setForeground(new java.awt.Color(47, 50, 58));

        SendButton.setBackground(new java.awt.Color(70, 73, 76));
        SendButton.setForeground(new java.awt.Color(133, 189, 191));
        SendButton.setText("Send");
        SendButton.setBorder(null);
        SendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendUnhover(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendHover(evt);
            }
        });
        SendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SendButtonActionPerformed(evt);
            }
        });

        ConvoScroll.setBackground(new java.awt.Color(204, 255, 204));
        ConvoScroll.setBorder(null);
        ConvoScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ConvoScroll.setPreferredSize(new java.awt.Dimension(302, 223));

        ConvoBox.setBackground(new java.awt.Color(70, 73, 76));
        ConvoBox.setLayout(new javax.swing.BoxLayout(ConvoBox, javax.swing.BoxLayout.PAGE_AXIS));
        ConvoScroll.setViewportView(ConvoBox);
        ConvoBox.getAccessibleContext().setAccessibleParent(jScrollPane1);

        TopicConvoName.setForeground(new java.awt.Color(139, 137, 130));
        TopicConvoName.setText("No Topic Selected...");

        javax.swing.GroupLayout ConvoPaneLayout = new javax.swing.GroupLayout(ConvoPane);
        ConvoPane.setLayout(ConvoPaneLayout);
        ConvoPaneLayout.setHorizontalGroup(
                ConvoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ConvoPaneLayout.createSequentialGroup()
                                .addGroup(ConvoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(ConvoPaneLayout.createSequentialGroup()
                                                .addComponent(TopicConvoName)
                                                .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(ConvoPaneLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(ConvoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(ConvoScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 442, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(ConvoPaneLayout.createSequentialGroup()
                                                                .addGroup(ConvoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(MessageSep)
                                                                        .addComponent(MessageField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(SendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addContainerGap())
        );
        ConvoPaneLayout.setVerticalGroup(
                ConvoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(ConvoPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(TopicConvoName, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(ConvoScroll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(ConvoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(ConvoPaneLayout.createSequentialGroup()
                                                .addComponent(MessageField, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(MessageSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(SendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        javax.swing.GroupLayout MainPaneLayout = new javax.swing.GroupLayout(MainPane);
        MainPane.setLayout(MainPaneLayout);
        MainPaneLayout.setHorizontalGroup(
                MainPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(MainPaneLayout.createSequentialGroup()
                                .addComponent(UtilityPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ConvoPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        MainPaneLayout.setVerticalGroup(
                MainPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(UtilityPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ConvoPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getContentPane().add(MainPane, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        myClient.ConnectionRequest(myClient.getUsername(), null, myClient.getServerHost(), "disconnect");
        this.dispose();
        Caller.setVisible(true);
    }

    private void AddTopicActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        String TopicName = TopicNameField.getText();
        try {
            myClient.AddTopicRequest(TopicName);
        } catch (RemoteException e) {
            System.err.println("Couldn't add topic...");
        }
        updateTopic();
        pack();
    }

    private void SendButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // Add a textpane to combooBox
        String message = MessageField.getText();
        if(message.isEmpty()) return;
        try {
            myClient.MessageRequest(new MessageClass(myClient.getUsername(), message), TopicConvoName.getText());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        updateConvo();
        updateTopic();
    }

    private void sendHover(java.awt.event.MouseEvent evt) {
        // TODO add your handling code here:
        SendButton.setBackground(new Color(76,92,104));
    }

    private void sendUnhover(java.awt.event.MouseEvent evt) {
        // TODO add your handling code here:
        SendButton.setBackground(new Color(70,73,76));
    }



    private void TopicNameFieldActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void TopicNameFieldKeyPressed(java.awt.event.KeyEvent evt) {
        // TODO add your handling code here:
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws UnknownHostException {
        /*User u = new User("127.0.0.1");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DashFrame(u, "").setVisible(true);
            }
        });
*/
    }

    // Variables declaration - do not modify
    private javax.swing.JButton AddTopic;
    private javax.swing.JPanel ConvoBox;
    private javax.swing.JPanel ConvoPane;
    private javax.swing.JScrollPane ConvoScroll;
    private javax.swing.JPanel MainPane;
    private javax.swing.JTextField MessageField;
    private javax.swing.JSeparator MessageSep;
    private javax.swing.JButton SendButton;
    private javax.swing.JPanel SubBox;
    private javax.swing.JTextField TopicNameField;
    private javax.swing.JPanel UtilityPane;
    private javax.swing.JButton DisconnectButton;
    private javax.swing.JLabel TopicConvoName;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    // End of variables declaration
}
