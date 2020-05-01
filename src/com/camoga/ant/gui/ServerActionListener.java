package com.camoga.ant.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.camoga.ant.test.net.Client;

public class ServerActionListener implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "Connect to Server":
			JPanel panel = new JPanel();
			if(Client.client.logged) {
				JOptionPane.showConfirmDialog(Window.f, "You are already logged as " + Client.client.username, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
			} else if(Client.properties.getProperty("username") == null){
				JTextField username = new JTextField(20);
				JPasswordField pw = new JPasswordField();
				JCheckBox staylogged = new JCheckBox();
				panel.setLayout(new GridLayout(3, 2));
				panel.add(new JLabel("Username: "));
				panel.add(username);
				panel.add(new JLabel("Password: "));
				panel.add(pw);
				panel.add(staylogged, -1);
				int option = JOptionPane.showConfirmDialog(Window.f, panel, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
				if(option == JOptionPane.OK_OPTION) {
					char[] chars = null;
					byte[] password = null;
					try {
						chars = pw.getPassword();
						password = new byte[chars.length];
						for(int i = 0; i < chars.length; i++) {
							password[i] = (byte) chars[i];
						}
						MessageDigest md = MessageDigest.getInstance("SHA-256");
						byte[] hash = md.digest(password);
						
						String strhash = Client.toHexString(hash);
						
						if(staylogged.isSelected()) {
							Client.properties.setProperty("username", username.getText());
							Client.properties.setProperty("hash", strhash);
							Client.properties.store(new FileOutputStream("langton.properties"), null);
						}
						Client.client.login(username.getText(), strhash);
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						Arrays.fill(chars, (char) 0);
						Arrays.fill(password, (byte)0);			
					}
					
				}
			}
			break;
		case "Settings":
			break;
		case "Get Assignments":
			Client.client.getAssigment(20);
			break;
		}
	}

}
