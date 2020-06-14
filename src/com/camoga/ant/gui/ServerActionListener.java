package com.camoga.ant.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
			if(Client.logged) {
				JOptionPane.showConfirmDialog(Window.f, "You are already logged as " + Client.username, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
			} else if(Client.properties.getProperty("username") == null || Client.properties.getProperty("hash") == null){
				Object[] options = new Object[] {"Cancel", "Register", "Login"};
				
				JTextField username = new JTextField(20);
				JPasswordField pw = new JPasswordField();
				panel.setLayout(new GridLayout(2, 2));
				panel.add(new JLabel("Username: "));
				panel.add(username);
				panel.add(new JLabel("Password: "));
				panel.add(pw);
				int option = JOptionPane.showOptionDialog(Window.f, panel, "Connect to Server", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
				
				if(option == JOptionPane.CANCEL_OPTION) { //Login
					String user = username.getText();
					String hash = Client.hash(pw.getPassword());
					Client.client.login(user, hash);
				} else if(option == JOptionPane.NO_OPTION) { //Register
					panel.removeAll();
					JPasswordField pw2 = new JPasswordField();
					panel.setLayout(new GridLayout(3,2));
					panel.add(new JLabel("Username: "));
					panel.add(username);
					panel.add(new JLabel("Password: "));
					panel.add(pw);
					panel.add(new JLabel("Repeat password: "));
					panel.add(pw2);
					
					options = new Object[] {"Cancel", "Register"};
					option = JOptionPane.showOptionDialog(Window.f, panel, "Connect to Server", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
					
					if(option==JOptionPane.NO_OPTION) { // register
						if(!Arrays.equals(pw.getPassword(), pw2.getPassword())) {
							JOptionPane.showInputDialog(Window.f, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						} else {
							String user = username.getText();
							String hash = Client.hash(pw.getPassword());
							Client.client.register(user, hash);
						}
					}
				}
			}
			break;
		case "Settings":
			break;
		}
	}
}