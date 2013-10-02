package com.redhat.victims.plugin.eclipse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

import com.redhat.victims.VictimsConfig;
import com.redhat.victims.VictimsException;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.plugin.eclipse.handler.SettingsCallback;
/**
 * Popup settings menu for specifying settings used by the Victims
 * Scanner.
 * @author kurt
 */
public class VictimsOptionMenu extends JFrame {


	private static final long serialVersionUID = -8821070859297752881L;
	/* GUI related fields */
	private JPanel contentPane;
	private JTextField baseUrlTxt;
	private JTextField entryPointTxt;
	private JTextField jdbcDriverTxt;
	private JTextField jdbcUrlTxt;
	private JTextField jdbcUserTxt;
	private JTextField jdbcPassTxt;
	private JComboBox updatesCombo;
	private JComboBox metadataCombo;
	private JComboBox fingerprintCombo;
	private ILog log;
	/* Stores the settings received from GUI */
	private Map<String,String> settings;
	/* Callback object */
	private SettingsCallback settingsHandler;

	/**
	 * Create the frame and contents.
	 * @param sh Object to callback to.
	 */
	public VictimsOptionMenu(SettingsCallback sh) {
		settingsHandler = sh;
		log = Activator.getDefault().getLog();
		settings = new HashMap<String,String>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 305, 340);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblBaseUrl = new JLabel("BaseUrl");
		lblBaseUrl.setBounds(22, 12, 70, 15);
		contentPane.add(lblBaseUrl);
		
		JLabel lblEntryPoint = new JLabel("entryPoint");
		lblEntryPoint.setBounds(22, 39, 75, 15);
		contentPane.add(lblEntryPoint);
		
		JLabel lblMetadata = new JLabel("metadata");
		lblMetadata.setBounds(22, 66, 70, 15);
		contentPane.add(lblMetadata);
		
		JLabel lblFingerprint = new JLabel("fingerprint");
		lblFingerprint.setBounds(22, 93, 84, 15);
		contentPane.add(lblFingerprint);
		
		JLabel lblUpdates = new JLabel("updates");
		lblUpdates.setBounds(22, 120, 70, 15);
		contentPane.add(lblUpdates);
		
		JLabel lblJdbcDriver = new JLabel("jdbcDriver");
		lblJdbcDriver.setBounds(22, 147, 75, 15);
		contentPane.add(lblJdbcDriver);
		
		JLabel lblJdbcUrl = new JLabel("jdbcUrl");
		lblJdbcUrl.setBounds(22, 174, 70, 15);
		contentPane.add(lblJdbcUrl);
		
		JLabel lblJdbcUser = new JLabel("jdbcUser");
		lblJdbcUser.setBounds(22, 201, 70, 15);
		contentPane.add(lblJdbcUser);
		
		JLabel lblJdbcPass = new JLabel("jdbcPass");
		lblJdbcPass.setBounds(22, 228, 70, 15);
		contentPane.add(lblJdbcPass);
		
		baseUrlTxt = new JTextField();
		baseUrlTxt.setText("http://www.victi.ms/");
		baseUrlTxt.setBounds(115, 12, 160, 19);
		contentPane.add(baseUrlTxt);
		baseUrlTxt.setColumns(10);
		
		entryPointTxt = new JTextField();
		entryPointTxt.setText("service/");
		entryPointTxt.setBounds(115, 39, 160, 19);
		contentPane.add(entryPointTxt);
		entryPointTxt.setColumns(10);
		
		metadataCombo = new JComboBox();
		metadataCombo.setModel(new DefaultComboBoxModel(new String[] {"warning", "fatal", "disabled"}));
		metadataCombo.setSelectedIndex(0);
		metadataCombo.setMaximumRowCount(3);
		metadataCombo.setBounds(115, 66, 120, 24);
		contentPane.add(metadataCombo);
		
		fingerprintCombo = new JComboBox();
		fingerprintCombo.setModel(new DefaultComboBoxModel(new String[] {"warning", "fatal", "disabled"}));
		fingerprintCombo.setSelectedIndex(1);
		fingerprintCombo.setMaximumRowCount(3);
		fingerprintCombo.setBounds(115, 93, 120, 24);
		contentPane.add(fingerprintCombo);
		
		updatesCombo = new JComboBox();
		updatesCombo.setModel(new DefaultComboBoxModel(new String[] {"auto", "daily", "offline"}));
		updatesCombo.setSelectedIndex(0);
		updatesCombo.setMaximumRowCount(3);
		updatesCombo.setBounds(115, 120, 120, 24);
		contentPane.add(updatesCombo);
		
		jdbcDriverTxt = new JTextField();
		jdbcDriverTxt.setText(VictimsDB.defaultDriver());
		jdbcDriverTxt.setBounds(115, 147, 160, 19);
		contentPane.add(jdbcDriverTxt);
		jdbcDriverTxt.setColumns(10);
		
		jdbcUrlTxt = new JTextField();
		jdbcUrlTxt.setText(VictimsDB.defaultURL());
		jdbcUrlTxt.setBounds(115, 174, 160, 19);
		contentPane.add(jdbcUrlTxt);
		jdbcUrlTxt.setColumns(10);
		
		jdbcUserTxt = new JTextField();
		jdbcUserTxt.setText("victims");
		jdbcUserTxt.setBounds(115, 201, 160, 19);
		contentPane.add(jdbcUserTxt);
		jdbcUserTxt.setColumns(10);
		
		jdbcPassTxt = new JTextField();
		jdbcPassTxt.setText("victims");
		jdbcPassTxt.setColumns(10);
		jdbcPassTxt.setBounds(115, 226, 160, 19);
		contentPane.add(jdbcPassTxt);
		
		JButton applyBtn = new JButton("Apply");
		applyBtn.setBounds(157, 276, 117, 25);
		contentPane.add(applyBtn);
		applyBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				applySettings();
			}
		});
		
		JButton CancelBtn = new JButton("Cancel");
		CancelBtn.setBounds(22, 276, 117, 25);
		contentPane.add(CancelBtn);
		CancelBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				closeFrame();
			}
		});
	}
	
	/**
	 * Performed when apply is pressed. Retrieves values
	 * from text fields and combo boxes and stores them in a hashmap
	 * using VictimsConfig keys.
	 * @return Hashmap of settings.
	 */
	public void applySettings(){
		try {
			String baseUrl = baseUrlTxt.getText();
			String entryPoint = entryPointTxt.getText();
			String jdbcDriver = jdbcDriverTxt.getText();
			String jdbcUrl = jdbcUrlTxt.getText();
			String jdbcUser = jdbcUserTxt.getText();
			String jdbcPass = jdbcPassTxt.getText();
			String metadata = (String)metadataCombo.getSelectedItem();
			String fingerprint = (String)fingerprintCombo.getSelectedItem();
			String updates = (String)updatesCombo.getSelectedItem();
			
			settings.put(VictimsConfig.Key.URI, baseUrl);
			settings.put(VictimsConfig.Key.ENTRY,entryPoint);
			settings.put(VictimsConfig.Key.DB_DRIVER,jdbcDriver);
			settings.put(VictimsConfig.Key.DB_URL,jdbcUrl);
			settings.put(VictimsConfig.Key.DB_USER,jdbcUser);
			settings.put(VictimsConfig.Key.DB_PASS,jdbcPass);
			settings.put(Settings.METADATA, metadata);
			settings.put(Settings.FINGERPRINT, fingerprint);
			settings.put(Settings.UPDATE_DATABASE, updates);
			this.setVisible(false);

			settingsHandler.callbackSettings();
		} catch (NullPointerException np){
			np.printStackTrace();
		//	log.log(0, np.getMessage());
			JOptionPane.showMessageDialog(this, "Please make sure all fields are filled.");
		} catch (VictimsException ve){
			//The exception train is getting kind of bad now :(
			log.log(new Status(Status.ERROR, Activator.PLUGIN_ID,
					ve.getMessage()));
			ve.printStackTrace();
		}
	}
	
	/**
	 * @return Settings for Victims connection
	 */
	public Map<String,String> getSettings(){
		return settings;
	}
	
	/**
	 * Called when cancel button is pressed.
	 * Hides the frame.
	 */
	private void closeFrame(){
		this.setVisible(false);
	}

}