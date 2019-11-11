import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

public class Main extends JFrame {

	private List<Data> datas = new ArrayList<>();
	private JTabbedPane tab;
	private SearchInfoPanel sip;
	private JLabel tableName;
	public Main() {
		setTitle("�������� �м� ���α׷�");
		setSize(1500, 1000);
		setLayout(new FlowLayout());
		createMenu();
		
		tab = new JTabbedPane();
		tab.setPreferredSize(new Dimension(1000, 800));
		tableName = new JLabel("");
		sip = new SearchInfoPanel(this);
	
		add(tab);
		add(new JLabel("���� ���� : "));
		add(tableName);
		add(sip);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public String getTableName() {
		return tableName.getText();
	}
	
	public List<Data> getDatas() {
		return datas;
	}
	
	
	public void setDatas(List<Data> datas, String duration) {
		
		if(datas != null) this.datas = datas;
		tab.removeAll();
		
		DataTablePanel dtp = new DataTablePanel();
		if(duration.equals("��ü")) {
			tab.addTab("��ü", dtp);
			for (Data data : this.datas) dtp.add(data);
		} else {

			int datelen = duration.equals("��") ? 4 : 6; // 
			
			tab.addTab(this.datas.get(0).getData(0).substring(0, datelen), dtp);
			for (Data data : this.datas) {
				if (!(data.getData(0).substring(0, datelen).equals(tab.getTitleAt(tab.getTabCount() - 1)))) {
					dtp.initPlaceList();
					dtp = new DataTablePanel();
					tab.addTab(data.getData(0).substring(0, datelen), dtp);
				}
				dtp.add(data);
			}
		}
		dtp.initPlaceList();
	}
	
	public void createMenu() { // �޴� ����
		JMenuBar mb = new JMenuBar();
		
		JMenu[] menus = {new JMenu("����"), new JMenu("������ ���")};
		String[][] items = {
				{"����", "����->���� ����", "����->���� ����", "�ʱ�ȭ", "����"},
				{"���� �׷���", "������ �׷���"}
		};

		MenuActionListener listener = new MenuActionListener();
		for (int i=0; i<menus.length; i++) {
			for(int j=0; j < items[i].length; j++) {
				JMenuItem item = new JMenuItem(items[i][j]);
				item.addActionListener(listener);
				menus[i].add(item);
			}
			mb.add(menus[i]);
		}
		
		setJMenuBar(mb);
	}
	
	private class MenuActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			switch(e.getActionCommand()) {
				case "����":
					open(); // DB ���� ������ -> ȭ���� ǥ�� ���, �Ǵ� csv ���� ��� ȭ���� ǥ�� ���
					break;
				case "����->���� ����":
					saveFileToServer(); // �� ��ǻ�� csv ���� -> DB ���� ���常 ����
					break;
				case "����->���� ����":
					saveServerToFile(); // ���� -> ���� ���常 ����
					break;
				case "�ʱ�ȭ":
					tableName.setText("");
					tab.removeAll();
					init();
					break;
				case "����":
					System.exit(0);
					break;
				default: // ����, ������ �޴�
					showGraphDialog(e.getActionCommand());
					break;
			}
		}
	}
	
	public void showGraphDialog(String graph) {
		if(getTableName().length() != 0) new GraphDialog(graph, datas);
		else JOptionPane.showMessageDialog(null, "�����͸� �����ּ���.");
	}
	
	public void init() {
		datas.clear();
		sip.init();
	}
	
	public void open() {
		
		// ���� ���� ������� ���� ����ó��
		
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setLayout(new FlowLayout()); // ���̾�α� ����
		
		JList list = new JList(Request.showTables());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // ����Ʈ ����
		
		JButton ok = new JButton("Ȯ��");
		JButton cancel = new JButton("���");
		ok.addActionListener(e -> {
			
			String value = (String) list.getSelectedValue();
			if(value == null) return;
			init();
			dialog.dispose();
			setDatas(Request.openData(value, null, null), "��ü");
			tableName.setText(value);
		}); // Ȯ�ν� �̺�Ʈ ó��
		cancel.addActionListener(e -> dialog.dispose()); // ��ҽ� ����
		
		dialog.add(new JScrollPane(list));
		dialog.add(ok);
		dialog.add(cancel);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public void saveServerToFile() {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setLayout(new FlowLayout()); // ���̾�α� ����
		
		JList list = new JList(Request.showTables());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // ����Ʈ ����
		
		JButton ok = new JButton("Ȯ��");
		JButton cancel = new JButton("���");
		ok.addActionListener(e -> {
			FileDialog fd = new FileDialog(this, "����", FileDialog.SAVE);
			fd.setDirectory(".");
			fd.setVisible(true);
			
			if(fd.getFile() == null) return; // �Է¾ȵǾ��ų�, csv���� �ƴϸ� ����
			String path = fd.getDirectory() + fd.getFile();
			if(!getType(path).equals("csv")) {
				JOptionPane.showMessageDialog(null, "csv���Ϸ� �������ּ���.");
				return;
			}
			
			dialog.dispose();
			String value = (String) list.getSelectedValue();
			if(value == null) return;
			Request.outputData(value, path);
		}); // Ȯ�ν� �̺�Ʈ ó��
		cancel.addActionListener(e -> dialog.dispose()); // ��ҽ� ����
		
		dialog.add(new JScrollPane(list));
		dialog.add(ok);
		dialog.add(cancel);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public String getType(String path) { // Ȯ���ڸ� ��ȯ
		return path.substring(path.lastIndexOf(".") + 1).toLowerCase();
	}
	
	public void saveFileToServer() { // ���� �޴�
		FileDialog dialog = new FileDialog(this, "����", FileDialog.LOAD);
		dialog.setDirectory(".");
		dialog.setVisible(true);
		
		if(dialog.getFile() == null) return; // ���ϰ����°ž����� ����
		
		String path = dialog.getDirectory() + dialog.getFile();

		if(!getType(path).equals("csv")) {
			JOptionPane.showMessageDialog(null, "csv ������ �ƴմϴ�.");
			return;
		}
		
		String name = JOptionPane.showInputDialog("������ �̸��� �������ּ���.");
		
		Request.inputData(path, name);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}

}
