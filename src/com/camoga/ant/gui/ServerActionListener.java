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

import com.camoga.ant.net.Client;

public class ServerActionListener implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
		case "Connect to Server":
			JPanel panel = new JPanel();
			if(Client.client.logged) {
				JOptionPane.showConfirmDialog(Window.f, "You are already logged as " + Client.client.username, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
			} else if(Client.properties.getProperty("username") == null){
				Object[] options = new Object[] {"Cancel", "Register", "Login"};
				
				JTextField username = new JTextField(20);
				JPasswordField pw = new JPasswordField();
				JCheckBox staylogged = new JCheckBox();
				panel.setLayout(new GridLayout(3, 2));
				panel.add(new JLabel("Username: "));
				panel.add(username);
				panel.add(new JLabel("Password: "));
				panel.add(pw);
				panel.add(new JLabel("Keep me logged in: "));
				panel.add(staylogged);
				int option = JOptionPane.showOptionDialog(Window.f, panel, "Connect to Server", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
				switch(option) {
				case JOptionPane.YES_OPTION:
					System.out.println("yes");
					break;
				case JOptionPane.CANCEL_OPTION:
					System.out.println("cancel");
					break;
				case JOptionPane.NO_OPTION:
					System.out.println("no");
					break;
				}
				
				if(option == JOptionPane.CANCEL_OPTION) {
					Client.client.login(username.getText(), storeCredentials(pw, username.getText(), staylogged));
				} else if(option == JOptionPane.NO_OPTION) {
					panel.removeAll();
					JPasswordField pw2 = new JPasswordField();
					panel.setLayout(new GridLayout(4,2));
					panel.add(new JLabel("Username: "));
					panel.add(username);
					panel.add(new JLabel("Password: "));
					panel.add(pw);
					panel.add(new JLabel("Repeat password: "));
					panel.add(pw2);
					panel.add(new JLabel("Keep me logged in: "));
					panel.add(staylogged);
					
					options = new Object[] {"Cancel", "Register"};
					option = JOptionPane.showOptionDialog(Window.f, panel, "Connect to Server", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
					
					if(option==JOptionPane.NO_OPTION) { // register
						if(!Arrays.equals(pw.getPassword(), pw2.getPassword())) {
							JOptionPane.showInputDialog(Window.f, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						} else {
							Client.client.register(username.getText(), storeCredentials(pw, username.getText(), staylogged));
						}
					}
				}
			}
			break;
		case "Settings":
			break;
		}
	}

	private String storeCredentials(JPasswordField pw, String username, JCheckBox staylogged) {
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
				Client.properties.setProperty("username", username);
				Client.properties.setProperty("hash", strhash);
				Client.properties.store(new FileOutputStream("langton.properties"), null);
			}
			return strhash;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Arrays.fill(chars, (char) 0);
			Arrays.fill(password, (byte)0);			
		}
		return null;
	}

}
