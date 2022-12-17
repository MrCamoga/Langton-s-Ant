package com.camoga.ant.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Optional;

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
			} else {
				Object[] options = new Object[] {"Login","Cancel"};
				
				JTextField username = new JTextField(Optional.of(Client.properties.getProperty("username")).orElse(""),24);
				JPasswordField pw = new JPasswordField(Optional.of(Client.properties.getProperty("secrettoken")).orElse(""),32);
				panel.setLayout(new GridLayout(2, 2));
				panel.add(new JLabel("Username: "));
				panel.add(username);
				panel.add(new JLabel("Secret token: "));
				panel.add(pw);
				int option = JOptionPane.showOptionDialog(Window.f, panel, "Connect to Server", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);
				
				if(option == JOptionPane.YES_OPTION) {
					String user = username.getText();
					String hash = new String(pw.getPassword());
					Client.client.login(user, hash);
				}
			}
			break;
		case "Settings":
			break;
		case "Send Data":
			Client.sendAssignmentResult();
			break;
		}
	}
}