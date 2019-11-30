import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class Main extends JFrame {

	private Datas datas;
	private JTabbedPane tab;
	private SearchInfoPanel sip;
	private JLabel tableName;
	private JButton insertData;
	private JButton deleteData;

	public Main() {
		setTitle("�������� �м� ���α׷�");
		setSize(1450, 1000);
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
		
		insertData = new JButton("�߰�");
		deleteData = new JButton("����");
		insertData.setVisible(false);
		deleteData.setVisible(false);
		
		insertData.addActionListener(e -> insertButton());
		deleteData.addActionListener(e -> deleteButton());
		
		add(insertData);
		add(deleteData);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public String getTableName() {
		return tableName.getText();
	}
	
	public Datas getDatas() {
		return datas;
	}
	
	public void setDatas(Datas datas) { // ����� ������ ������ ����. �ʱ⿡�� ��ü �ǿ� �����
		sip.init();
		tab.removeAll();
		this.datas = datas;
		List<Data> list = datas.getDatas();
		Map<String, List<Data>> m = new HashMap<>();
		for (Data data : list) {
			if(m.get(data.getPlace()) == null) m.put(data.getPlace(), new ArrayList<>());
			m.get(data.getPlace()).add(data);
		}
		tab.addTab("��ü", new DataTablePanel(m));
		((DataTablePanel)tab.getComponentAt(0)).getTable().setEnabled(true);
		((DataTablePanel)tab.getComponentAt(0)).getTableModel().addTableModelListener(e ->update(e.getFirstRow(), e.getColumn()));
	}
	
	public void setTableDatas(String duration) { // ��, �� ������ư �������� ���� �̺�Ʈ ó��
		
		removeTab();

		List<Data> list = datas.getDatas();
		Map<String, List<Data>> m = new HashMap<>();

		if (!duration.equals("��ü") && !list.isEmpty()) {
			int datelen = duration.equals("��") ? 4 : 6;
			for (int i=0; i<list.size() - 1; i++) {
				if(m.get(list.get(i).getPlace()) == null) m.put(list.get(i).getPlace(), new ArrayList<>());
				m.get(list.get(i).getPlace()).add(list.get(i));
				
				if (!(list.get(i).getDate().substring(0, datelen).equals(list.get(i+1).getDate().substring(0, datelen)))) {
					tab.addTab(list.get(i).getDate().substring(0, datelen), new DataTablePanel(m));
					m = new HashMap<>();
				}
			}
			if(m.get(list.get(list.size()-1).getPlace()) == null) m.put(list.get(list.size()-1).getPlace(), new ArrayList<>());
			m.get(list.get(list.size()-1).getPlace()).add(list.get(list.size()-1)); // ������ ������ �ѹ� ����
			tab.addTab(list.get(list.size()-1).getDate().substring(0, datelen), new DataTablePanel(m));
		}
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
					insertData.setVisible(false);
					deleteData.setVisible(false);
					tableName.setText("");
					tab.removeAll();
					init();
					break;
				case "����":
					System.exit(0);
					break;
				default: // ����, ������ �޴�
					showGraphDialog(e.getActionCommand());
			}
		}
	}
	
	public void showGraphDialog(String graph) {
		if(getTableName().length() != 0 && !datas.getDatas().isEmpty()) new GraphDialog(graph, ((DataTablePanel)tab.getComponentAt(0)).getDatas(), datas.getStart(), datas.getEnd());
		else JOptionPane.showMessageDialog(null, "�����͸� �����ּ���.");
	}
	
	public void init() {
		datas.getDatas().clear();
		sip.init();
	}
	
	public void open() {
		
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setLayout(new FlowLayout()); // ���̾�α� ����
		
		Vector<String> tables = new Vector<>();
		for(String s : Request.showTables()) tables.add(s);
		JList list = new JList(tables);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // ����Ʈ ����
		
		JButton ok = new JButton("Ȯ��");
		JButton cancel = new JButton("���");
		JButton remove = new JButton("����");
		
		remove.addActionListener(e -> {
			int i = list.getSelectedIndex();
			if(i == -1) return;
			Request.remove(tables.get(i));
			tables.remove(i);
			list.repaint();
		});
		ok.addActionListener(e -> {
			String value = (String) list.getSelectedValue();
			if(value == null) return;
			dialog.dispose();
			setDatas(Request.openData(value, null, null));
			setTableDatas("��ü");
			insertData.setVisible(true);
			deleteData.setVisible(true);
			tableName.setText(datas.getName());
		}); // Ȯ�ν� �̺�Ʈ ó��
		cancel.addActionListener(e -> dialog.dispose()); // ��ҽ� ����
		
		dialog.add(new JScrollPane(list));
		dialog.add(ok);
		dialog.add(cancel);
		dialog.add(remove);
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
	
	public void insertButton() {
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setSize(300, 550);
		dialog.setLayout(new FlowLayout());
		JLabel date = new JLabel("�����Ͻ�");
		JTextField dateText = new JTextField(20);
		JLabel place = new JLabel("�����Ҹ�");
		JTextField placeText = new JTextField(20);
		JLabel nitrogen = new JLabel("�̻�ȭ���ҳ�(ppm)");
		JTextField nitrogenText = new JTextField(20);
		JLabel ozone = new JLabel("������(ppm)");
		JTextField ozoneText = new JTextField(20);
		JLabel carbon = new JLabel("�̻�ȭź�ҳ�(ppm)");
		JTextField carbonText = new JTextField(20);
		JLabel gas = new JLabel("��Ȳ�갡��(ppm)");
		JTextField gasText = new JTextField(20);
		JLabel dust = new JLabel("�̼�����(��/��)");
		JTextField dustText = new JTextField(20);
		JLabel ultraDust = new JLabel("�ʹ̼�����(��/��)");
		JTextField ultraDustText = new JTextField(20);
		JButton submit = new JButton("Ȯ��");
		submit.addActionListener(ee -> {
			
			if (dateText.getText().length() == 0 || placeText.getText().length() == 0 ||
					nitrogenText.getText().length() == 0 || ozoneText.getText().length() == 0 ||
					carbonText.getText().length() == 0 || gasText.getText().length() == 0 ||
					dustText.getText().length() == 0 || ultraDustText.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "�����͸� �Է����ּ���.");
				return;
			} else if(dateText.getText().length() != 8 || !isNum(dateText.getText())) {
				JOptionPane.showMessageDialog(null, "��¥ ������ �ùٸ��� �ʽ��ϴ�.");
				return;
			} else if (binarySearchDate(((DataTablePanel)tab.getComponentAt(0)).getDatas().get(placeText.getText()), dateText.getText(), false) != -1) {
				JOptionPane.showMessageDialog(null, "�� ��¥�� �ش��ϴ� ��� �����ʹ� �ϳ��� �Է� �����մϴ�.");
				return;
			} else {
				try {
					Double.parseDouble(nitrogenText.getText());
					Double.parseDouble(ozoneText.getText());
					Double.parseDouble(carbonText.getText());
					Double.parseDouble(gasText.getText());
					Double.parseDouble(dustText.getText());
					Double.parseDouble(ultraDustText.getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "�������� ������ ������ �ùٸ��� �ʽ��ϴ�.");
					return;
				}
			}
			
			int pk = Request.insertData(new String[] {dateText.getText(), placeText.getText(), nitrogenText.getText(), ozoneText.getText(), carbonText.getText(), gasText.getText(), dustText.getText(), ultraDustText.getText()}, datas.getName());
			if(pk != -1) {
				int index = binarySearchDate(datas.getDatas(), dateText.getText(), true);
				index = dateText.getText().compareTo(datas.getDatas().get(index).getDate()) > 0 ? index + 1 : index; 

				datas.getDatas().add(index, new Data(pk, dateText.getText(), placeText.getText(), nitrogenText.getText(), ozoneText.getText(), carbonText.getText(), gasText.getText(), dustText.getText(), ultraDustText.getText()));
				
				datas.setStart(datas.getDatas().get(0).getDate());
				datas.setEnd(datas.getDatas().get(datas.getDatas().size()-1).getDate());
				setDatas(datas);
			} else JOptionPane.showMessageDialog(null, "������ �߰� ����");
		});
		dialog.add(date);
		dialog.add(dateText);
		dialog.add(place);
		dialog.add(placeText);
		dialog.add(nitrogen);
		dialog.add(nitrogenText);
		dialog.add(ozone);
		dialog.add(ozoneText);
		dialog.add(carbon);
		dialog.add(carbonText);
		dialog.add(gas);
		dialog.add(gasText);
		dialog.add(dust);
		dialog.add(dustText);
		dialog.add(ultraDust);
		dialog.add(ultraDustText);
		dialog.add(submit);
		
		dialog.setVisible(true);
	}
	
	public void deleteButton() {
		
		DataTablePanel t = (DataTablePanel)tab.getSelectedComponent();
		
		if (tab.getSelectedIndex() != 0 ) {
			JOptionPane.showMessageDialog(null, "������ ��ü �ǿ��� ���� �����մϴ�.");
			return;
		} else if (t.getTable().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(null, "���õ� �����Ͱ� �����ϴ�.");
			return;
		}

		int pk = -1;
		
		String place = (String)t.getTable().getValueAt(t.getTable().getSelectedRow(), 1);
		String date = (String)t.getTable().getValueAt(t.getTable().getSelectedRow(), 0);
		
		List<Data> d = t.getDatas().get(place);
		int index = binarySearchDate(d, date, false); // ���� �����Ϳ��� ����
		if(index == -1) return;
		pk = d.get(index).getId();
		d.remove(index);
		
		t.getTableModel().removeRow(t.getRealIndex(index, place));
		t.updateCount();
		
		index = binarySearchDate(datas.getDatas(), date, false);
		if(index == -1) return;
		
		int l=index, r=index+1; // �������� ����, ���������� ���� ����. ���� �����Ϳ��� �����ϱ� ���� pk ã��
		while(l>=0 && datas.getDatas().get(l).getId() != pk && datas.getDatas().get(l--).getDate().compareTo(date) < 0);
		if (datas.getDatas().get(l).getId() == pk) datas.getDatas().remove(l);
		else {
			while(r < datas.getDatas().size() && datas.getDatas().get(r).getId() != pk && datas.getDatas().get(r++).getDate().compareTo(date) > 0);
			datas.getDatas().remove(r);
		}
		
		if(!datas.getDatas().isEmpty()) { // �Ⱓ�� ���۳�¥, ����¥ ������
			datas.setStart(datas.getDatas().get(0).getDate());
			datas.setEnd(datas.getDatas().get(datas.getDatas().size()-1).getDate());
		}
		Request.deleteData(pk, datas.getName());
		t.getTable().clearSelection();
		removeTab();
		sip.init();
	}
	
	public void update(int row, int col) {
		
		DataTablePanel t = (DataTablePanel)tab.getComponentAt(0);
		
		if(col == -1) return;
		else
			try {
				Double.parseDouble(((String)t.getTableModel().getValueAt(row, col)));
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "�ùٸ� ���� �����͸� �Է����ּ���. �߸��� �����ʹ� ������� �ʽ��ϴ�.");
				return;
			}

		List<Data> d = t.getDatas().get(t.getTableModel().getValueAt(row, 1));
		int index = binarySearchDate(d, (String)t.getTableModel().getValueAt(row, 0), false);
		
		if(index == -1) return;
		int pk = d.get(index).getId();
		String val = (String)t.getTableModel().getValueAt(row, col);
		d.get(index).setData(col, val);
		Request.updateData(pk, datas.getName(), val, col);
		removeTab();
		sip.init();
	}
	
	public int binarySearchDate(List<Data> d, String date, boolean flag) {
		//flag�� false�� ���� �� -1 ��ȯ, true�� ���� �� �� ��ġ ��ȯ.
		if(d == null || d.isEmpty()) return -1;
		
		int l = 0;
		int r = d.size() - 1;
		int mid = -1;
		while(l<=r) { 
			mid = (l+r)/2;
			if(d.get(mid).getDate().compareTo(date) < 0) l = mid+1;
			else if(d.get(mid).getDate().compareTo(date) > 0) r = mid-1;
			else return mid;
		}
		return flag ? mid : -1;
	}
	
	public boolean isNum(String text) {
		for (char c : text.toCharArray())
			if('0' > c || c > '9') return false;
		return true;
	}
	
	public void removeTab() { // ��ü �� �����ϰ� ����
		for(int i=tab.getTabCount() -1 ; i>0; i--) tab.remove(i);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Main();
	}

}
